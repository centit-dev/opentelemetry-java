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

package io.opentelemetry.sdk.logs.data;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.logs.Severity;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.LogLimits;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.trace.SpanContext;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Log definition as described in <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/logs/data-model.md">OpenTelemetry
 * Log Data Model</a>.
 *
 * @since 1.27.0
 */
@Immutable
public interface LogRecordData {

  /** Returns the resource of this log. */
  Resource getResource();

  /** Returns the instrumentation scope that generated this log. */
  InstrumentationLibraryInfo getInstrumentationScopeInfo();

  /** Returns the timestamp at which the log record occurred, in epoch nanos. */
  long getTimestampEpochNanos();

  /** Returns the timestamp at which the log record was observed, in epoch nanos. */
  long getObservedTimestampEpochNanos();

  /** Return the span context for this log, or {@link SpanContext#getInvalid()} if unset. */
  SpanContext getSpanContext();

  /** Returns the severity for this log. */
  Severity getSeverity();

  /** Returns the severity text for this log, or null if unset. */
  @Nullable
  String getSeverityText();

  /** Returns the body for this log. */
  Body getBody();

  /** Returns the attributes for this log, or {@link Attributes#empty()} if unset. */
  Attributes getAttributes();

  /**
   * Returns the total number of attributes that were recorded on this log.
   *
   * <p>This number may be larger than the number of attributes that are attached to this log, if
   * the total number recorded was greater than the configured maximum value. See {@link
   * LogLimits#getMaxNumberOfAttributes()}.
   */
  int getTotalAttributeCount();
}
