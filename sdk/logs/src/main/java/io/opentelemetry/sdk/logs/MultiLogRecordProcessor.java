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

package io.opentelemetry.sdk.logs;

import io.grpc.Context;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation of {@link LogRecordProcessor} that forwards all logs to a list of {@link
 * LogRecordProcessor}s.
 */
final class MultiLogRecordProcessor implements LogRecordProcessor {

  private final List<LogRecordProcessor> logRecordProcessors;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);

  /**
   * Create a new {@link MultiLogRecordProcessor}.
   *
   * @param logRecordProcessorsList list of log processors to forward logs to
   * @return a multi log processor instance
   */
  static LogRecordProcessor create(List<LogRecordProcessor> logRecordProcessorsList) {
    return new MultiLogRecordProcessor(
        new ArrayList<>(
            Objects.requireNonNull(logRecordProcessorsList, "logRecordProcessorsList")));
  }

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {
    for (LogRecordProcessor logRecordProcessor : logRecordProcessors) {
      logRecordProcessor.onEmit(context, logRecord);
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    if (isShutdown.getAndSet(true)) {
      return CompletableResultCode.ofSuccess();
    }
    List<CompletableResultCode> results = new ArrayList<>(logRecordProcessors.size());
    for (LogRecordProcessor logRecordProcessor : logRecordProcessors) {
      results.add(logRecordProcessor.shutdown());
    }
    return CompletableResultCode.ofAll(results);
  }

  @Override
  public CompletableResultCode forceFlush() {
    List<CompletableResultCode> results = new ArrayList<>(logRecordProcessors.size());
    for (LogRecordProcessor logRecordProcessor : logRecordProcessors) {
      results.add(logRecordProcessor.forceFlush());
    }
    return CompletableResultCode.ofAll(results);
  }

  private MultiLogRecordProcessor(List<LogRecordProcessor> logRecordProcessorsList) {
    this.logRecordProcessors = logRecordProcessorsList;
  }

  @Override
  public void close() throws IOException {
    LogRecordProcessorHelper.close(this);
  }

  public void addProcessor(LogRecordProcessor processor) {
    logRecordProcessors.add(processor);
  }
}
