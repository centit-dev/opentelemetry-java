/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.logs.export;

import io.grpc.Context;
import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.AsynchronousInstrument;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongValueObserver;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.MeterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.DaemonThreadFactory;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the {@link LogRecordProcessor} that batches logs exported by the SDK then
 * pushes them to the exporter pipeline.
 *
 * <p>All logs reported by the SDK implementation are first added to a synchronized queue (with a
 * {@code maxQueueSize} maximum size, if queue is full logs are dropped). Logs are exported either
 * when there are {@code maxExportBatchSize} pending logs or {@code scheduleDelayNanos} has passed
 * since the last export finished.
 *
 * @since 1.27.0
 */
public final class BatchLogRecordProcessor implements LogRecordProcessor {

  private static final String WORKER_THREAD_NAME =
      BatchLogRecordProcessor.class.getSimpleName() + "_WorkerThread";
  private static final String LOG_RECORD_PROCESSOR_TYPE_LABEL = "processorType";
  private static final String LOG_RECORD_PROCESSOR_DROPPED_LABEL = "dropped";
  private static final String LOG_RECORD_PROCESSOR_TYPE_VALUE =
      BatchLogRecordProcessor.class.getSimpleName();

  private final Worker worker;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Returns a new Builder for {@link BatchLogRecordProcessor}.
   *
   * @param logRecordExporter the {@link LogRecordExporter} to which the Logs are pushed
   * @return a new {@link BatchLogRecordProcessor}.
   * @throws NullPointerException if the {@code logRecordExporter} is {@code null}.
   */
  public static BatchLogRecordProcessorBuilder builder(LogRecordExporter logRecordExporter) {
    return new BatchLogRecordProcessorBuilder(logRecordExporter);
  }

  BatchLogRecordProcessor(
      LogRecordExporter logRecordExporter,
      MeterProvider meterProvider,
      long scheduleDelayNanos,
      int maxQueueSize,
      int maxExportBatchSize,
      long exporterTimeoutNanos) {
    this.worker =
        new Worker(
            logRecordExporter,
            meterProvider,
            scheduleDelayNanos,
            maxExportBatchSize,
            exporterTimeoutNanos,
            new ArrayBlockingQueue<ReadWriteLogRecord>(
                maxQueueSize)); // TODO: use JcTools.newFixedSizeQueue(..)
    Thread workerThread = new DaemonThreadFactory(WORKER_THREAD_NAME).newThread(worker);
    workerThread.start();
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    if (logRecord == null) {
      return;
    }
    worker.addLog(logRecord);
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    return worker.shutdown();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return worker.forceFlush();
  }

  /**
   * Return the processor's configured {@link LogRecordExporter}.
   *
   * @since 1.37.0
   */
  public LogRecordExporter getLogRecordExporter() {
    return worker.logRecordExporter;
  }

  // Visible for testing
  List<LogRecordData> getBatch() {
    return worker.batch;
  }

  @Override
  public String toString() {
    return "BatchLogRecordProcessor{"
        + "logRecordExporter="
        + worker.logRecordExporter
        + ", scheduleDelayNanos="
        + worker.scheduleDelayNanos
        + ", maxExportBatchSize="
        + worker.maxExportBatchSize
        + ", exporterTimeoutNanos="
        + worker.exporterTimeoutNanos
        + '}';
  }

  @Override
  public void close() throws IOException {
    LogRecordProcessorHelper.close(this);
  }

  // Worker is a thread that batches multiple logs and calls the registered LogRecordExporter to
  // export
  // the data.
  private static final class Worker implements Runnable {

    private static final Logger logger = Logger.getLogger(Worker.class.getName());

    private final LongCounter processedLogsCounter;
    private final Labels droppedAttrs;
    private final Labels exportedAttrs;

    private final LogRecordExporter logRecordExporter;
    private final long scheduleDelayNanos;
    private final int maxExportBatchSize;
    private final long exporterTimeoutNanos;

    private long nextExportTime;

    private final Queue<ReadWriteLogRecord> queue;
    // When waiting on the logs queue, exporter thread sets this atomic to the number of more
    // logs it needs before doing an export. Writer threads would then wait for the queue to reach
    // logsNeeded size before notifying the exporter thread about new entries.
    // Integer.MAX_VALUE is used to imply that exporter thread is not expecting any signal. Since
    // exporter thread doesn't expect any signal initially, this value is initialized to
    // Integer.MAX_VALUE.
    private final AtomicInteger logsNeeded = new AtomicInteger(Integer.MAX_VALUE);
    private final BlockingQueue<Boolean> signal;
    private final AtomicReference<CompletableResultCode> flushRequested = new AtomicReference<>();
    private volatile boolean continueWork = true;
    private final ArrayList<LogRecordData> batch;

    private Worker(
        LogRecordExporter logRecordExporter,
        MeterProvider meterProvider,
        long scheduleDelayNanos,
        int maxExportBatchSize,
        long exporterTimeoutNanos,
        final Queue<ReadWriteLogRecord> queue) {
      this.logRecordExporter = logRecordExporter;
      this.scheduleDelayNanos = scheduleDelayNanos;
      this.maxExportBatchSize = maxExportBatchSize;
      this.exporterTimeoutNanos = exporterTimeoutNanos;
      this.queue = queue;
      this.signal = new ArrayBlockingQueue<>(1);
      Meter meter = meterProvider.get("io.opentelemetry.sdk.logs");
      LongValueObserver valueObserver =
          meter
              .longValueObserverBuilder("queueSize")
              .setDescription("The number of items queued")
              .setUnit("1")
              .build();
      valueObserver.setCallback(
          new AsynchronousInstrument.Callback<AsynchronousInstrument.LongResult>() {
            @Override
            public void update(AsynchronousInstrument.LongResult result) {
              result.observe(
                  queue.size(),
                  Labels.of(LOG_RECORD_PROCESSOR_TYPE_LABEL, LOG_RECORD_PROCESSOR_TYPE_VALUE));
            }
          });
      processedLogsCounter =
          meter
              .longCounterBuilder("processedLogs")
              .setUnit("1")
              .setDescription(
                  "The number of logs processed by the BatchLogRecordProcessor. "
                      + "[dropped=true if they were dropped due to high throughput]")
              .build();
      droppedAttrs =
          Labels.of(
              LOG_RECORD_PROCESSOR_TYPE_LABEL,
              LOG_RECORD_PROCESSOR_TYPE_VALUE,
              LOG_RECORD_PROCESSOR_DROPPED_LABEL,
              "true");
      exportedAttrs =
          Labels.of(
              LOG_RECORD_PROCESSOR_TYPE_LABEL,
              LOG_RECORD_PROCESSOR_TYPE_VALUE,
              LOG_RECORD_PROCESSOR_DROPPED_LABEL,
              "false");

      this.batch = new ArrayList<>(this.maxExportBatchSize);
    }

    private void addLog(ReadWriteLogRecord logData) {
      if (!queue.offer(logData)) {
        processedLogsCounter.add(1, droppedAttrs);
      } else {
        if (queue.size() >= logsNeeded.get()) {
          signal.offer(true);
        }
      }
    }

    @Override
    public void run() {
      updateNextExportTime();

      while (continueWork) {
        if (flushRequested.get() != null) {
          flush();
        }
        while (!queue.isEmpty() && batch.size() < maxExportBatchSize) {
          batch.add(queue.poll().toLogRecordData());
        }
        if (batch.size() >= maxExportBatchSize || System.nanoTime() >= nextExportTime) {
          exportCurrentBatch();
          updateNextExportTime();
        }
        if (queue.isEmpty()) {
          try {
            long pollWaitTime = nextExportTime - System.nanoTime();
            if (pollWaitTime > 0) {
              logsNeeded.set(maxExportBatchSize - batch.size());
              signal.poll(pollWaitTime, TimeUnit.NANOSECONDS);
              logsNeeded.set(Integer.MAX_VALUE);
            }
          } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
          }
        }
      }
    }

    private void flush() {
      int logsToFlush = queue.size();
      while (logsToFlush > 0) {
        ReadWriteLogRecord logRecord = queue.poll();
        assert logRecord != null;
        batch.add(logRecord.toLogRecordData());
        logsToFlush--;
        if (batch.size() >= maxExportBatchSize) {
          exportCurrentBatch();
        }
      }
      exportCurrentBatch();
      CompletableResultCode flushResult = flushRequested.get();
      if (flushResult != null) {
        flushResult.succeed();
        flushRequested.set(null);
      }
    }

    private void updateNextExportTime() {
      nextExportTime = System.nanoTime() + scheduleDelayNanos;
    }

    private CompletableResultCode shutdown() {
      final CompletableResultCode result = new CompletableResultCode();

      final CompletableResultCode flushResult = forceFlush();
      flushResult.whenComplete(
          new Runnable() {
            @Override
            public void run() {
              continueWork = false;
              final CompletableResultCode shutdownResult = logRecordExporter.shutdown();
              shutdownResult.whenComplete(
                  new Runnable() {
                    @Override
                    public void run() {
                      if (!flushResult.isSuccess() || !shutdownResult.isSuccess()) {
                        result.fail();
                      } else {
                        result.succeed();
                      }
                    }
                  });
            }
          });

      return result;
    }

    private CompletableResultCode forceFlush() {
      CompletableResultCode flushResult = new CompletableResultCode();
      // we set the atomic here to trigger the worker loop to do a flush of the entire queue.
      if (flushRequested.compareAndSet(null, flushResult)) {
        signal.offer(true);
      }
      CompletableResultCode possibleResult = flushRequested.get();
      // there's a race here where the flush happening in the worker loop could complete before we
      // get what's in the atomic. In that case, just return success, since we know it succeeded in
      // the interim.
      return possibleResult == null ? CompletableResultCode.ofSuccess() : possibleResult;
    }

    private void exportCurrentBatch() {
      if (batch.isEmpty()) {
        return;
      }

      try {
        CompletableResultCode result =
            logRecordExporter.export(Collections.unmodifiableList(batch));
        result.join(exporterTimeoutNanos, TimeUnit.NANOSECONDS);
        if (result.isSuccess()) {
          processedLogsCounter.add(batch.size(), exportedAttrs);
        } else {
          logger.log(Level.FINE, "Exporter failed");
        }
      } catch (RuntimeException e) {
        logger.log(Level.WARNING, "Exporter threw an Exception", e);
      } finally {
        batch.clear();
      }
    }
  }
}
