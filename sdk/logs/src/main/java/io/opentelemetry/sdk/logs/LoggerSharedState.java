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

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.funcitonal.Supplier;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;

/**
 * Represents shared state and config between all {@link SdkLogger}s created by the same {@link
 * SdkLogRecordDataProvider}.
 */
final class LoggerSharedState {
  private final Object lock = new Object();
  private final Resource resource;
  private final Supplier<LogLimits> logLimitsSupplier;
  private LogRecordProcessor logRecordProcessor;
  private final Clock clock;
  @Nullable private volatile CompletableResultCode shutdownResult = null;

  LoggerSharedState(
      Resource resource,
      Supplier<LogLimits> logLimitsSupplier,
      LogRecordProcessor logRecordProcessor,
      Clock clock) {
    this.resource = resource;
    this.logLimitsSupplier = logLimitsSupplier;
    this.logRecordProcessor = logRecordProcessor;
    this.clock = clock;
  }

  Resource getResource() {
    return resource;
  }

  LogLimits getLogLimits() {
    return logLimitsSupplier.get();
  }

  LogRecordProcessor getLogRecordProcessor() {
    return logRecordProcessor;
  }

  void addLogRecordProcessor(LogRecordProcessor logRecordProcessor) {
    if (this.logRecordProcessor instanceof NoopLogRecordProcessor) {
      this.logRecordProcessor =
          LogRecordProcessor.LogRecordProcessorHelper.composite(logRecordProcessor);
    } else if (this.logRecordProcessor instanceof MultiLogRecordProcessor) {
      ((MultiLogRecordProcessor) this.logRecordProcessor).addProcessor(logRecordProcessor);
    }
  }

  Clock getClock() {
    return clock;
  }

  boolean hasBeenShutdown() {
    return shutdownResult != null;
  }

  CompletableResultCode shutdown() {
    synchronized (lock) {
      if (shutdownResult != null) {
        return shutdownResult;
      }
      shutdownResult = logRecordProcessor.shutdown();
      return shutdownResult;
    }
  }
}
