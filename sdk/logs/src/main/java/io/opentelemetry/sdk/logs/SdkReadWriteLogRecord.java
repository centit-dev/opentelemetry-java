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

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.logs.Severity;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.trace.SpanContext;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class SdkReadWriteLogRecord implements ReadWriteLogRecord {

  private final LogLimits logLimits;
  private final Resource resource;
  private final InstrumentationLibraryInfo instrumentationScopeInfo;
  private final long timestampEpochNanos;
  private final long observedTimestampEpochNanos;
  private final SpanContext spanContext;
  private final Severity severity;
  @Nullable private final String severityText;
  private final Body body;
  private final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private AttributesMap attributes;

  private SdkReadWriteLogRecord(
      LogLimits logLimits,
      Resource resource,
      InstrumentationLibraryInfo instrumentationScopeInfo,
      long timestampEpochNanos,
      long observedTimestampEpochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      Body body,
      @Nullable AttributesMap attributes) {
    this.logLimits = logLimits;
    this.resource = resource;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.timestampEpochNanos = timestampEpochNanos;
    this.observedTimestampEpochNanos = observedTimestampEpochNanos;
    this.spanContext = spanContext;
    this.severity = severity;
    this.severityText = severityText;
    this.body = body;
    this.attributes = attributes;
  }

  /** Create the log record with the given configuration. */
  static SdkReadWriteLogRecord create(
      LogLimits logLimits,
      Resource resource,
      InstrumentationLibraryInfo instrumentationScopeInfo,
      long timestampEpochNanos,
      long observedTimestampEpochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      Body body,
      @Nullable AttributesMap attributes) {
    return new SdkReadWriteLogRecord(
        logLimits,
        resource,
        instrumentationScopeInfo,
        timestampEpochNanos,
        observedTimestampEpochNanos,
        spanContext,
        severity,
        severityText,
        body,
        attributes);
  }

  @Override
  public ReadWriteLogRecord setAttribute(String key, AttributeValue value) {
    if (key == null || key.isEmpty() || value == null) {
      return this;
    }
    synchronized (lock) {
      if (attributes == null) {
        attributes = new AttributesMap(logLimits.getMaxNumberOfAttributes());
      }
      attributes.put(key, value);
    }
    return this;
  }

  private Attributes getImmutableAttributes() {
    synchronized (lock) {
      if (attributes == null || attributes.isEmpty()) {
        return Attributes.empty();
      }
      return Attributes.newBuilder(attributes).build();
    }
  }

  @Override
  public LogRecordData toLogRecordData() {
    synchronized (lock) {
      return SdkLogRecordData.create(
          resource,
          instrumentationScopeInfo,
          timestampEpochNanos,
          observedTimestampEpochNanos,
          spanContext,
          severity,
          severityText,
          body,
          getImmutableAttributes(),
          attributes == null ? 0 : attributes.getTotalAddedValues());
    }
  }
}
