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

package io.opentelemetry.sdk.logs.internal;

import io.grpc.Context;
import io.opentelemetry.logs.LogRecordDataProvider;
import io.opentelemetry.logs.Logger;
import io.opentelemetry.logs.LoggerBuilder;
import io.opentelemetry.logs.Severity;
import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.SystemClock;

/**
 * SDK implementation for {@link EventLoggerProvider}.
 *
 * <p>Delegates all calls to the configured
 * {@link LogRecordDataProvider}, and its {@link LoggerBuilder}s,
 * {@link Logger}s.
 */
public final class SdkEventLoggerProvider implements EventLoggerProvider {

  private static final Severity DEFAULT_SEVERITY = Severity.INFO;

  private final LogRecordDataProvider delegateLoggerProvider;
  private final Clock clock;

  private SdkEventLoggerProvider(LogRecordDataProvider delegateLoggerProvider, Clock clock) {
    this.delegateLoggerProvider = delegateLoggerProvider;
    this.clock = clock;
  }

  /**
   * Create a {@link SdkEventLoggerProvider} which delegates to the {@code delegateLoggerProvider}.
   */
  public static SdkEventLoggerProvider create(LogRecordDataProvider delegateLogRecordDataProvider) {
    return new SdkEventLoggerProvider(delegateLogRecordDataProvider, SystemClock.getInstance());
  }

  /**
   * Create a {@link SdkEventLoggerProvider} which delegates to the {@code delegateLoggerProvider}.
   */
  public static SdkEventLoggerProvider create(
      LogRecordDataProvider delegateLogRecordDataProvider, Clock clock) {
    return new SdkEventLoggerProvider(delegateLogRecordDataProvider, clock);
  }

  @Override
  public EventLogger get(String instrumentationScopeName) {
    return eventLoggerBuilder(instrumentationScopeName).build();
  }

  @Override
  public EventLoggerBuilder eventLoggerBuilder(String instrumentationScopeName) {
    return new SdkEventLoggerBuilder(
        clock, delegateLoggerProvider.loggerBuilder(instrumentationScopeName));
  }

  private static class SdkEventLoggerBuilder implements EventLoggerBuilder {

    private final Clock clock;
    private final LoggerBuilder delegateLoggerBuilder;

    private SdkEventLoggerBuilder(Clock clock, LoggerBuilder delegateLoggerBuilder) {
      this.clock = clock;
      this.delegateLoggerBuilder = delegateLoggerBuilder;
    }

    @Override
    public EventLoggerBuilder setSchemaUrl(String schemaUrl) {
      delegateLoggerBuilder.setSchemaUrl(schemaUrl);
      return this;
    }

    @Override
    public EventLoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
      delegateLoggerBuilder.setInstrumentationVersion(instrumentationScopeVersion);
      return this;
    }

    @Override
    public EventLogger build() {
      return new SdkEventLogger(clock, delegateLoggerBuilder.build());
    }
  }

  private static class SdkEventLogger implements EventLogger {

    private final Clock clock;
    private final Logger delegateLogger;

    private SdkEventLogger(Clock clock, Logger delegateLogger) {
      this.clock = clock;
      this.delegateLogger = delegateLogger;
    }

    @Override
    public EventBuilder builder(String eventName) {
      return new SdkEventBuilder(
          clock,
          delegateLogger
              .logRecordBuilder()
              .setSeverity(DEFAULT_SEVERITY)
              .setContext(Context.current()),
          eventName);
    }
  }
}
