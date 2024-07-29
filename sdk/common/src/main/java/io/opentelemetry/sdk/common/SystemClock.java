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

package io.opentelemetry.sdk.common;

import java.util.concurrent.TimeUnit;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class SystemClock implements Clock {

  private static final SystemClock INSTANCE = new SystemClock();

  private SystemClock() {}

  /** Returns a {@link SystemClock}. */
  public static Clock getInstance() {
    return INSTANCE;
  }

  @Override
  public long now() {
    return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
  }

  @Override
  public long nanoTime() {
    return System.nanoTime();
  }

  @Override
  public String toString() {
    return "SystemClock{}";
  }
}
