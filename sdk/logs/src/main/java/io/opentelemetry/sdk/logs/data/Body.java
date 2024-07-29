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

import javax.annotation.concurrent.Immutable;

/**
 * This represents all the possible values for a log message body. A {@code Body} can currently only
 * have 1 type of values: {@code String}, represented through {@code Body.Type}. This class will
 * likely be extended in the future to include additional body types supported by the OpenTelemetry
 * log data model.
 *
 * @since 1.27.0
 */
@Immutable
public interface Body {

  /** An enum that represents all the possible value types for an {@code Body}. */
  enum Type {
    EMPTY,
    STRING
    // TODO (jack-berg): Add ANY_VALUE type when API for setting body to AnyValue is stable
    // ANY_VALUE
  }

  /** Returns the String value of this {@code Body}. */
  String asString();

  /** Returns the type of the {@code Body}. */
  Type getType();
}
