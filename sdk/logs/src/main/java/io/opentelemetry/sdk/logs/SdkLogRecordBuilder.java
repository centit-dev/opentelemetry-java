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
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.logs.LogRecordBuilder;
import io.opentelemetry.logs.Severity;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.EmptyBody;
import io.opentelemetry.sdk.logs.internal.AnyValueBody;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.TracingContextUtils;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** SDK implementation of {@link LogRecordBuilder}. */
final class SdkLogRecordBuilder implements LogRecordBuilder {

  private final LoggerSharedState loggerSharedState;
  private final LogLimits logLimits;

  private final InstrumentationLibraryInfo instrumentationScopeInfo;
  private long timestampEpochNanos;
  private long observedTimestampEpochNanos;
  @Nullable private Context context;
  private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
  @Nullable private String severityText;
  private Body body = EmptyBody.INSTANCE;
  @Nullable private AttributesMap attributes;

  SdkLogRecordBuilder(
      LoggerSharedState loggerSharedState, InstrumentationLibraryInfo instrumentationScopeInfo) {
    this.loggerSharedState = loggerSharedState;
    this.logLimits = loggerSharedState.getLogLimits();
    this.instrumentationScopeInfo = instrumentationScopeInfo;
  }

  @Override
  public LogRecordBuilder setTimestamp(long timestamp, TimeUnit unit) {
    this.timestampEpochNanos = unit.toNanos(timestamp);
    return this;
  }

  @Override
  public LogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit) {
    this.observedTimestampEpochNanos = unit.toNanos(timestamp);
    return this;
  }

  @Override
  public LogRecordBuilder setContext(Context context) {
    this.context = context;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setSeverity(Severity severity) {
    this.severity = severity;
    return this;
  }

  @Override
  public LogRecordBuilder setSeverityText(String severityText) {
    this.severityText = severityText;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setBody(String body) {
    this.body = AnyValueBody.create(body);
    return this;
  }

  @Override
  public LogRecordBuilder setAllAttributes(Attributes attributes) {
    return null;
  }

  @Override
  public LogRecordBuilder setAttribute(String key, AttributeValue value) {
    if (key == null || key.isEmpty() || value == null) {
      return this;
    }
    if (this.attributes == null) {
      this.attributes = new AttributesMap(logLimits.getMaxNumberOfAttributes());
    }
    this.attributes.put(key, value);
    return this;
  }

  @Override
  public void emit() {
    if (loggerSharedState.hasBeenShutdown()) {
      return;
    }
    Context context = this.context == null ? Context.current() : this.context;
    long observedTimestampEpochNanos =
        this.observedTimestampEpochNanos == 0
            ? this.loggerSharedState.getClock().now()
            : this.observedTimestampEpochNanos;
    Span span = TracingContextUtils.getSpan(context);
    loggerSharedState
        .getLogRecordProcessor()
        .onEmit(
            context,
            SdkReadWriteLogRecord.create(
                loggerSharedState.getLogLimits(),
                loggerSharedState.getResource(),
                instrumentationScopeInfo,
                timestampEpochNanos,
                observedTimestampEpochNanos,
                span.getContext(),
                severity,
                severityText,
                body,
                attributes));
  }
}
