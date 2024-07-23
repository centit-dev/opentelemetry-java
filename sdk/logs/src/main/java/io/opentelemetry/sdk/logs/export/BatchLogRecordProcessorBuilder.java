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

import static io.opentelemetry.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.metrics.DefaultMeterProvider;
import io.opentelemetry.metrics.MeterProvider;
import java.util.concurrent.TimeUnit;

/**
 * Builder class for {@link BatchLogRecordProcessor}.
 *
 * @since 1.27.0
 */
public final class BatchLogRecordProcessorBuilder {

  // Visible for testing
  static final long DEFAULT_SCHEDULE_DELAY_MILLIS = 1000;
  // Visible for testing
  static final int DEFAULT_MAX_QUEUE_SIZE = 2048;
  // Visible for testing
  static final int DEFAULT_MAX_EXPORT_BATCH_SIZE = 512;
  // Visible for testing
  static final int DEFAULT_EXPORT_TIMEOUT_MILLIS = 30_000;

  private final LogRecordExporter logRecordExporter;
  private long scheduleDelayNanos = TimeUnit.MILLISECONDS.toNanos(DEFAULT_SCHEDULE_DELAY_MILLIS);
  private int maxQueueSize = DEFAULT_MAX_QUEUE_SIZE;
  private int maxExportBatchSize = DEFAULT_MAX_EXPORT_BATCH_SIZE;
  private long exporterTimeoutNanos = TimeUnit.MILLISECONDS.toNanos(DEFAULT_EXPORT_TIMEOUT_MILLIS);
  private MeterProvider meterProvider = DefaultMeterProvider.getInstance();

  BatchLogRecordProcessorBuilder(LogRecordExporter logRecordExporter) {
    this.logRecordExporter = requireNonNull(logRecordExporter, "logRecordExporter");
  }

  /**
   * Sets the delay interval between two consecutive exports. If unset, defaults to {@value
   * DEFAULT_SCHEDULE_DELAY_MILLIS}ms.
   */
  public BatchLogRecordProcessorBuilder setScheduleDelay(long delay, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(delay >= 0, "delay must be non-negative");
    scheduleDelayNanos = unit.toNanos(delay);
    return this;
  }

  /**
   * Sets the delay interval between two consecutive exports. If unset, defaults to {@value
   * DEFAULT_SCHEDULE_DELAY_MILLIS}ms.
   */
  public BatchLogRecordProcessorBuilder setScheduleDelay(long delay) {
    return setScheduleDelay(delay, TimeUnit.NANOSECONDS);
  }

  // Visible for testing
  long getScheduleDelayNanos() {
    return scheduleDelayNanos;
  }

  /**
   * Sets the maximum time an export will be allowed to run before being cancelled. If unset,
   * defaults to {@value DEFAULT_EXPORT_TIMEOUT_MILLIS}ms.
   */
  public BatchLogRecordProcessorBuilder setExporterTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    exporterTimeoutNanos = unit.toNanos(timeout);
    return this;
  }

  /**
   * Sets the maximum time an export will be allowed to run before being cancelled. If unset,
   * defaults to {@value DEFAULT_EXPORT_TIMEOUT_MILLIS}ms.
   */
  public BatchLogRecordProcessorBuilder setExporterTimeout(long timeout) {
    return setExporterTimeout(timeout, TimeUnit.NANOSECONDS);
  }

  // Visible for testing
  long getExporterTimeoutNanos() {
    return exporterTimeoutNanos;
  }

  /**
   * Sets the maximum number of Logs that are kept in the queue before start dropping. More memory
   * than this value may be allocated to optimize queue access.
   *
   * <p>Default value is {@code 2048}.
   *
   * @param maxQueueSize the maximum number of Logs that are kept in the queue before start
   *     dropping.
   * @return this.
   * @see BatchLogRecordProcessorBuilder#DEFAULT_MAX_QUEUE_SIZE
   */
  public BatchLogRecordProcessorBuilder setMaxQueueSize(int maxQueueSize) {
    this.maxQueueSize = maxQueueSize;
    return this;
  }

  // Visible for testing
  int getMaxQueueSize() {
    return maxQueueSize;
  }

  /**
   * Sets the maximum batch size for every export. This must be smaller or equal to {@code
   * maxQueueSize}.
   *
   * <p>Default value is {@code 512}.
   *
   * @param maxExportBatchSize the maximum batch size for every export.
   * @return this.
   * @see BatchLogRecordProcessorBuilder#DEFAULT_MAX_EXPORT_BATCH_SIZE
   */
  public BatchLogRecordProcessorBuilder setMaxExportBatchSize(int maxExportBatchSize) {
    checkArgument(maxExportBatchSize > 0, "maxExportBatchSize must be positive.");
    this.maxExportBatchSize = maxExportBatchSize;
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use to collect metrics related to batch export. If not set,
   * metrics will not be collected.
   */
  public BatchLogRecordProcessorBuilder setMeterProvider(MeterProvider meterProvider) {
    requireNonNull(meterProvider, "meterProvider");
    this.meterProvider = meterProvider;
    return this;
  }

  // Visible for testing
  int getMaxExportBatchSize() {
    return maxExportBatchSize;
  }

  /**
   * Returns a new {@link BatchLogRecordProcessor} that batches, then forwards them to the given
   * {@code logRecordExporter}.
   *
   * @return a new {@link BatchLogRecordProcessor}.
   */
  public BatchLogRecordProcessor build() {
    return new BatchLogRecordProcessor(
        logRecordExporter,
        meterProvider,
        scheduleDelayNanos,
        maxQueueSize,
        maxExportBatchSize,
        exporterTimeoutNanos);
  }
}
