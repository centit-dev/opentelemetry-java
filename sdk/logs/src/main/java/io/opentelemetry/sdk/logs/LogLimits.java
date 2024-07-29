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

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.funcitonal.Supplier;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds limits enforced during log recording.
 *
 * <p>Note: To allow dynamic updates of {@link LogLimits} you should register a {@link
 * java.util.function.Supplier} with {@link SdkLoggerProviderBuilder#setLogLimits(Supplier)} which
 * supplies dynamic configs when queried.
 *
 * @since 1.27.0
 */
@AutoValue
@Immutable
public abstract class LogLimits {

  private static final LogLimits DEFAULT = new LogLimitsBuilder().build();

  /** Returns the default {@link LogLimits}. */
  public static LogLimits getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link LogLimitsBuilder} to construct a {@link LogLimits}. */
  public static LogLimitsBuilder builder() {
    return new LogLimitsBuilder();
  }

  static LogLimits create(int maxNumAttributes, int maxAttributeLength) {
    return new AutoValue_LogLimits(maxNumAttributes, maxAttributeLength);
  }

  LogLimits() {}

  /**
   * Returns the max number of attributes per {@link LogRecordData}.
   *
   * @return the max number of attributes per {@link LogRecordData}.
   */
  public abstract int getMaxNumberOfAttributes();

  /**
   * Returns the max number of characters for string attribute values. For string array attribute
   * values, applies to each entry individually.
   *
   * @return the max number of characters for attribute strings.
   */
  public abstract int getMaxAttributeValueLength();

  /**
   * Returns a {@link LogLimitsBuilder} initialized to the same property values as the current
   * instance.
   *
   * @return a {@link LogLimitsBuilder} initialized to the same property values as the current
   *     instance.
   */
  public LogLimitsBuilder toBuilder() {
    return new LogLimitsBuilder()
        .setMaxNumberOfAttributes(getMaxNumberOfAttributes())
        .setMaxAttributeValueLength(getMaxAttributeValueLength());
  }
}
