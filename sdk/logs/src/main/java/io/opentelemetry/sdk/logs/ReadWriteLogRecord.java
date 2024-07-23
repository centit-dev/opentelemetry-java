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
import io.opentelemetry.common.ReadableKeyValuePairs;
import io.opentelemetry.sdk.logs.data.LogRecordData;

/**
 * A log record that can be read from and written to.
 *
 * @since 1.27.0
 */
public interface ReadWriteLogRecord {

  /**
   * Sets an attribute on the log record. If the log record previously contained a mapping for the
   * key, the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   */
  ReadWriteLogRecord setAttribute(String key, AttributeValue value);

  // TODO: add additional setters

  /** Return an immutable {@link LogRecordData} instance representing this log record. */
  LogRecordData toLogRecordData();

  // TODO: add additional log record accessors. Currently, all fields can be accessed indirectly via
  // #toLogRecordData() at the expense of additional allocations.

  class ReadWriteLogRecordHelper {

    /**
     * Sets attributes to the {@link ReadWriteLogRecord}. If the {@link ReadWriteLogRecord}
     * previously contained a mapping for any of the keys, the old values are replaced by the
     * specified values.
     *
     * @param attributes the attributes
     * @return this.
     * @since 1.31.0
     */
    static ReadWriteLogRecord setAllAttributes(
        final ReadWriteLogRecord record, Attributes attributes) {
      if (attributes == null || attributes.isEmpty()) {
        return record;
      }
      attributes.forEach(
          new ReadableKeyValuePairs.KeyValueConsumer<AttributeValue>() {
            @Override
            public void consume(String key, AttributeValue value) {
              record.setAttribute(key, value);
            }
          });
      return record;
    }
  }
}
