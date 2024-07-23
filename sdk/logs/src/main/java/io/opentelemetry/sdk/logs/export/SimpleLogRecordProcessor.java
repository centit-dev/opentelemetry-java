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

import static java.util.Objects.requireNonNull;

import io.grpc.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An implementation of the {@link LogRecordProcessor} that passes {@link LogRecordData} directly to
 * the configured exporter.
 *
 * <p>This processor will cause all logs to be exported directly as they finish, meaning each export
 * request will have a single log. Most backends will not perform well with a single log per request
 * so unless you know what you're doing, strongly consider using {@link BatchLogRecordProcessor}
 * instead, including in special environments such as serverless runtimes. {@link
 * SimpleLogRecordProcessor} is generally meant to for testing only.
 *
 * @since 1.27.0
 */
public final class SimpleLogRecordProcessor implements LogRecordProcessor {

  private static final Logger logger = Logger.getLogger(SimpleLogRecordProcessor.class.getName());

  private final LogRecordExporter logRecordExporter;
  private final Set<CompletableResultCode> pendingExports =
      Collections.newSetFromMap(new ConcurrentHashMap<CompletableResultCode, Boolean>());
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Returns a new {@link SimpleLogRecordProcessor} which exports logs to the {@link
   * LogRecordExporter} synchronously.
   *
   * <p>This processor will cause all logs to be exported directly as they finish, meaning each
   * export request will have a single log. Most backends will not perform well with a single log
   * per request so unless you know what you're doing, strongly consider using {@link
   * BatchLogRecordProcessor} instead, including in special environments such as serverless
   * runtimes. {@link SimpleLogRecordProcessor} is generally meant to for testing only.
   */
  public static LogRecordProcessor create(LogRecordExporter exporter) {
    requireNonNull(exporter, "exporter");
    return new SimpleLogRecordProcessor(exporter);
  }

  private SimpleLogRecordProcessor(LogRecordExporter logRecordExporter) {
    this.logRecordExporter = requireNonNull(logRecordExporter, "logRecordExporter");
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    try {
      List<LogRecordData> logs = Collections.singletonList(logRecord.toLogRecordData());
      final CompletableResultCode result = logRecordExporter.export(logs);
      pendingExports.add(result);
      result.whenComplete(
          new Runnable() {
            @Override
            public void run() {
              pendingExports.remove(result);
              if (!result.isSuccess()) {
                logger.log(Level.FINE, "Exporter failed");
              }
            }
          });
    } catch (RuntimeException e) {
      logger.log(Level.WARNING, "Exporter threw an Exception", e);
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    final CompletableResultCode result = new CompletableResultCode();

    final CompletableResultCode flushResult = forceFlush();
    flushResult.whenComplete(
        new Runnable() {
          @Override
          public void run() {
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

  @Override
  public CompletableResultCode forceFlush() {
    return CompletableResultCode.ofAll(pendingExports);
  }

  /**
   * Return the processor's configured {@link LogRecordExporter}.
   *
   * @since 1.37.0
   */
  public LogRecordExporter getLogRecordExporter() {
    return logRecordExporter;
  }

  @Override
  public String toString() {
    return "SimpleLogRecordProcessor{" + "logRecordExporter=" + logRecordExporter + '}';
  }

  @Override
  public void close() throws IOException {
    LogRecordProcessorHelper.close(this);
  }
}
