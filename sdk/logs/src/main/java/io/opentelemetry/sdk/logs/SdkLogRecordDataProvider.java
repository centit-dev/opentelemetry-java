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

import io.opentelemetry.logs.LogRecordDataProvider;
import io.opentelemetry.logs.Logger;
import io.opentelemetry.logs.LoggerBuilder;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ComponentRegistry;
import io.opentelemetry.sdk.logs.funcitonal.Supplier;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import io.opentelemetry.sdk.logs.internal.ScopeConfigurator;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import javax.annotation.Nullable;

/**
 * SDK implementation for {@link LogRecordDataProvider}.
 *
 * @since 1.27.0
 */
public final class SdkLogRecordDataProvider implements LogRecordDataProvider, Closeable {

  static final String DEFAULT_LOGGER_NAME = "unknown";
  private static final java.util.logging.Logger LOGGER =
      java.util.logging.Logger.getLogger(SdkLogRecordDataProvider.class.getName());

  private final LoggerSharedState sharedState;
  private final ComponentRegistry<SdkLogger> loggerComponentRegistry;
  private final ScopeConfigurator<LoggerConfig> loggerConfigurator;
  private boolean isNoopLogRecordProcessor;

  /**
   * Returns a new {@link SdkLoggerProviderBuilder} for {@link SdkLogRecordDataProvider}.
   *
   * @return a new builder instance
   */
  public static SdkLoggerProviderBuilder builder() {
    return new SdkLoggerProviderBuilder();
  }

  SdkLogRecordDataProvider(
      Resource resource,
      Supplier<LogLimits> logLimitsSupplier,
      List<LogRecordProcessor> processors,
      Clock clock,
      ScopeConfigurator<LoggerConfig> loggerConfigurator) {
    LogRecordProcessor logRecordProcessor =
        LogRecordProcessor.LogRecordProcessorHelper.composite(processors);
    this.sharedState =
        new LoggerSharedState(resource, logLimitsSupplier, logRecordProcessor, clock);
    this.loggerComponentRegistry =
        new ComponentRegistry<SdkLogger>() {
          @Override
          public SdkLogger newComponent(InstrumentationLibraryInfo instrumentationLibraryInfo) {
            return new SdkLogger(
                sharedState,
                instrumentationLibraryInfo,
                getLoggerConfig(instrumentationLibraryInfo));
          }
        };
    this.loggerConfigurator = loggerConfigurator;
    this.isNoopLogRecordProcessor = logRecordProcessor instanceof NoopLogRecordProcessor;
  }

  private LoggerConfig getLoggerConfig(InstrumentationLibraryInfo instrumentationScopeInfo) {
    LoggerConfig loggerConfig = loggerConfigurator.apply(instrumentationScopeInfo);
    return loggerConfig == null ? LoggerConfig.defaultConfig() : loggerConfig;
  }

  @Override
  public Logger get(String instrumentationScopeName) {
    return loggerComponentRegistry.get(instrumentationNameOrDefault(instrumentationScopeName));
  }

  @Override
  public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
    if (isNoopLogRecordProcessor) {
      return LogRecordDataProvider.LoggerProviderHelper.noop()
          .loggerBuilder(instrumentationScopeName);
    }
    return new SdkLoggerBuilder(
        loggerComponentRegistry, instrumentationNameOrDefault(instrumentationScopeName));
  }

  @Override
  public LogRecordDataProvider noop() {
    return LoggerProviderHelper.noop();
  }

  private static String instrumentationNameOrDefault(@Nullable String instrumentationScopeName) {
    if (instrumentationScopeName == null || instrumentationScopeName.isEmpty()) {
      LOGGER.fine("Logger requested without instrumentation scope name.");
      return DEFAULT_LOGGER_NAME;
    }
    return instrumentationScopeName;
  }

  /**
   * Request the active log processor to process all logs that have not yet been processed.
   *
   * @return a {@link CompletableResultCode} which is completed when the flush is finished
   */
  public CompletableResultCode forceFlush() {
    return sharedState.getLogRecordProcessor().forceFlush();
  }

  /**
   * Attempt to shut down the active log processor.
   *
   * @return a {@link CompletableResultCode} which is completed when the active log process has been
   *     shut down.
   */
  public CompletableResultCode shutdown() {
    if (sharedState.hasBeenShutdown()) {
      LOGGER.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return sharedState.shutdown();
  }

  public void addProcessor(LogRecordProcessor processor) {
    isNoopLogRecordProcessor = false;
    sharedState.addLogRecordProcessor(processor);
  }

  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }

  @Override
  public String toString() {
    return "SdkLoggerProvider{"
        + "clock="
        + sharedState.getClock()
        + ", resource="
        + sharedState.getResource()
        + ", logLimits="
        + sharedState.getLogLimits()
        + ", logRecordProcessor="
        + sharedState.getLogRecordProcessor()
        + '}';
  }
}
