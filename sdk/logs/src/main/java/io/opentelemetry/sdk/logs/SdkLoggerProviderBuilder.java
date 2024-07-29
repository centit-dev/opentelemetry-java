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

import static java.util.Objects.requireNonNull;

import io.grpc.Context;
import io.opentelemetry.logs.LogRecordBuilder;
import io.opentelemetry.logs.Logger;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.common.SystemClock;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.funcitonal.Predicate;
import io.opentelemetry.sdk.logs.funcitonal.Supplier;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;
import io.opentelemetry.sdk.logs.internal.ScopeConfigurator;
import io.opentelemetry.sdk.logs.internal.ScopeConfiguratorBuilder;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Builder class for {@link SdkLogRecordDataProvider} instances.
 *
 * @since 1.27.0
 */
public final class SdkLoggerProviderBuilder {

  private final List<LogRecordProcessor> logRecordProcessors = new ArrayList<>();
  private Resource resource = Resource.getDefault();
  private Supplier<LogLimits> logLimitsSupplier =
      new Supplier<LogLimits>() {
        @Override
        public LogLimits get() {
          return LogLimits.getDefault();
        }
      };
  private Clock clock = SystemClock.getInstance();
  private ScopeConfiguratorBuilder<LoggerConfig> loggerConfiguratorBuilder =
      LoggerConfig.configuratorBuilder();

  SdkLoggerProviderBuilder() {}

  /**
   * Assign a {@link Resource} to be attached to all {@link LogRecordData} created by {@link
   * Logger}s obtained from the {@link SdkLogRecordDataProvider}.
   *
   * @param resource the resource
   * @return this
   */
  public SdkLoggerProviderBuilder setResource(Resource resource) {
    requireNonNull(resource, "resource");
    this.resource = resource;
    return this;
  }

  /**
   * Merge a {@link Resource} with the current.
   *
   * @param resource {@link Resource} to merge with current.
   * @since 1.29.0
   */
  public SdkLoggerProviderBuilder addResource(Resource resource) {
    Objects.requireNonNull(resource, "resource");
    this.resource = this.resource.merge(resource);
    return this;
  }

  /**
   * Assign a {@link Supplier} of {@link LogLimits}. {@link LogLimits} will be retrieved each time a
   * {@link Logger#logRecordBuilder()} is called.
   *
   * <p>The {@code logLimitsSupplier} must be thread-safe and return immediately (no remote calls,
   * as contention free as possible).
   *
   * @param logLimitsSupplier the supplier that will be used to retrieve the {@link LogLimits} for
   *     every {@link LogRecordBuilder}.
   * @return this
   */
  public SdkLoggerProviderBuilder setLogLimits(Supplier<LogLimits> logLimitsSupplier) {
    requireNonNull(logLimitsSupplier, "logLimitsSupplier");
    this.logLimitsSupplier = logLimitsSupplier;
    return this;
  }

  /**
   * Add a log processor. {@link LogRecordProcessor#onEmit(Context, ReadWriteLogRecord)} will be
   * called each time a log is emitted by {@link Logger} instances obtained from the {@link
   * SdkLogRecordDataProvider}.
   *
   * @param processor the log processor
   * @return this
   */
  public SdkLoggerProviderBuilder addLogRecordProcessor(LogRecordProcessor processor) {
    requireNonNull(processor, "processor");
    logRecordProcessors.add(processor);
    return this;
  }

  /**
   * Assign a {@link Clock}.
   *
   * @param clock The clock to use for all temporal needs.
   * @return this
   */
  public SdkLoggerProviderBuilder setClock(Clock clock) {
    requireNonNull(clock, "clock");
    this.clock = clock;
    return this;
  }

  /**
   * Set the logger configurator, which computes {@link LoggerConfig} for each {@link
   * io.opentelemetry.sdk.common.InstrumentationLibraryInfo}.
   *
   * <p>Overrides any matchers added via {@link #addLoggerConfiguratorCondition(Predicate,
   * LoggerConfig)}.
   *
   * @see LoggerConfig#configuratorBuilder()
   */
  SdkLoggerProviderBuilder setLoggerConfigurator(
      ScopeConfigurator<LoggerConfig> loggerConfigurator) {
    this.loggerConfiguratorBuilder =
        ScopeConfigurator.ScopeConfiguratorHelper.toBuilder(loggerConfigurator);
    return this;
  }

  /**
   * Adds a condition to the logger configurator, which computes {@link LoggerConfig} for each
   * {@link InstrumentationLibraryInfo}.
   *
   * <p>Applies after any previously added conditions.
   *
   * <p>If {@link #setLoggerConfigurator(ScopeConfigurator)} was previously called, this condition
   * will only be applied if the {@link ScopeConfigurator#apply(Object)} returns null for the
   * matched {@link InstrumentationLibraryInfo}(s).
   *
   * @see ScopeConfiguratorBuilder#nameEquals(String)
   * @see ScopeConfiguratorBuilder#nameMatchesGlob(String)
   */
  SdkLoggerProviderBuilder addLoggerConfiguratorCondition(
      Predicate<InstrumentationLibraryInfo> scopeMatcher, LoggerConfig loggerConfig) {
    this.loggerConfiguratorBuilder.addCondition(scopeMatcher, loggerConfig);
    return this;
  }

  /**
   * Create a {@link SdkLogRecordDataProvider} instance.
   *
   * @return an instance configured with the provided options
   */
  public SdkLogRecordDataProvider build() {
    return new SdkLogRecordDataProvider(
        resource, logLimitsSupplier, logRecordProcessors, clock, loggerConfiguratorBuilder.build());
  }
}
