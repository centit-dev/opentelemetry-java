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

import io.opentelemetry.sdk.logs.data.Body;
import javax.annotation.concurrent.Immutable;

@Immutable
public final class AnyValueBody implements Body {

  private final String value;

  private AnyValueBody(String value) {
    this.value = value;
  }

  public static Body create(String value) {
    return new AnyValueBody(value);
  }

  @Override
  public Type getType() {
    return Type.STRING;
  }

  @Override
  public String asString() {
    return value;
  }

  @Override
  public String toString() {
    return "AnyValueBody{" + asString() + "}";
  }
}
