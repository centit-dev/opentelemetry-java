/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.logs;

import io.grpc.Context;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableKeyValuePairs;

import java.util.concurrent.TimeUnit;

/**
 * Used to construct and emit log records from a {@link Logger}.
 *
 * <p>Obtain a {@link Logger#logRecordBuilder()}, add properties using the setters, and emit the log
 * record by calling {@link #emit()}.
 *
 * @since 1.27.0
 */
public interface LogRecordBuilder {

  /**
   * Set the epoch {@code timestamp}, using the timestamp and unit.
   *
   * <p>The {@code timestamp} is the time at which the log record occurred. If unset, it will be set
   * to the current time when {@link #emit()} is called.
   */
  LogRecordBuilder setTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the epoch {@code observedTimestamp}, using the timestamp and unit.
   *
   * <p>The {@code observedTimestamp} is the time at which the log record was observed. If unset, it
   * will be set to the {@code timestamp}. {@code observedTimestamp} may be different from {@code
   * timestamp} if logs are being processed asynchronously (e.g. from a file or on a different
   * thread).
   */
  LogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit);

  /** Set the context. */
  LogRecordBuilder setContext(Context context);

  /** Set the severity. */
  LogRecordBuilder setSeverity(Severity severity);

  /** Set the severity text. */
  LogRecordBuilder setSeverityText(String severityText);

  /** Set the body string. */
  LogRecordBuilder setBody(String body);

  LogRecordBuilder setAllAttributes(Attributes attributes);

  /** Sets an attribute. */
  LogRecordBuilder setAttribute(String key, AttributeValue value);

  /** Emit the log record. */
  void emit();

  class LogRecordBuilderAttributesSetter {

    /**
     * Sets attributes. If the {@link LogRecordBuilder} previously contained a mapping
     * for any of the keys, the old values are replaced by the specified values.
     */
    static LogRecordBuilder setAllAttributes(
        final LogRecordBuilder builder, Attributes attributes) {
      if (attributes == null || attributes.isEmpty()) {
        return builder;
      }
      attributes.forEach(new ReadableKeyValuePairs.KeyValueConsumer<AttributeValue>() {
        @Override
        public void consume(String key, AttributeValue value) {
          builder.setAttribute(key, value);
        }
      });
      return builder;
    }
  }
}
