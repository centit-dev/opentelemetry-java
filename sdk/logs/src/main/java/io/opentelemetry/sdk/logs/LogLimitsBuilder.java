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

import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.logs.data.LogRecordData;

/**
 * Builder for {@link LogLimits}.
 *
 * @since 1.27.0
 */
public final class LogLimitsBuilder {

  private static final int DEFAULT_LOG_MAX_NUM_ATTRIBUTES = 128;
  private static final int DEFAULT_LOG_MAX_ATTRIBUTE_LENGTH = Integer.MAX_VALUE;

  private int maxNumAttributes = DEFAULT_LOG_MAX_NUM_ATTRIBUTES;
  private int maxAttributeValueLength = DEFAULT_LOG_MAX_ATTRIBUTE_LENGTH;

  LogLimitsBuilder() {}

  /**
   * Sets the max number of attributes per {@link LogRecordData}.
   *
   * @param maxNumberOfAttributes the max number of attributes per {@link LogRecordData}. Must be
   *     positive.
   * @return this.
   * @throws IllegalArgumentException if {@code maxNumberOfAttributes} is not positive.
   */
  public LogLimitsBuilder setMaxNumberOfAttributes(int maxNumberOfAttributes) {
    Utils.checkArgument(maxNumberOfAttributes > 0, "maxNumberOfAttributes must be greater than 0");
    this.maxNumAttributes = maxNumberOfAttributes;
    return this;
  }

  /**
   * Sets the max number of characters for string attribute values. For string array attribute
   * values, applies to each entry individually.
   *
   * @param maxAttributeValueLength the max number of characters for attribute strings. Must not be
   *     negative.
   * @return this.
   * @throws IllegalArgumentException if {@code maxAttributeValueLength} is negative.
   */
  public LogLimitsBuilder setMaxAttributeValueLength(int maxAttributeValueLength) {
    Utils.checkArgument(
        maxAttributeValueLength > -1, "maxAttributeValueLength must be non-negative");
    this.maxAttributeValueLength = maxAttributeValueLength;
    return this;
  }

  /** Builds and returns a {@link LogLimits} with the values of this builder. */
  public LogLimits build() {
    return LogLimits.create(maxNumAttributes, maxAttributeValueLength);
  }
}
