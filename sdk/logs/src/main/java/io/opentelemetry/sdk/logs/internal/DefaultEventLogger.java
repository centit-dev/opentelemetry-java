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

import io.grpc.Context;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.logs.Severity;
import java.util.concurrent.TimeUnit;

class DefaultEventLogger implements EventLogger {

  private static final EventLogger INSTANCE = new DefaultEventLogger();

  private DefaultEventLogger() {}

  static EventLogger getInstance() {
    return INSTANCE;
  }

  @Override
  public EventBuilder builder(String eventName) {
    return NoOpEventBuilder.INSTANCE;
  }

  private static class NoOpEventBuilder implements EventBuilder {

    public static final EventBuilder INSTANCE = new NoOpEventBuilder();

    @Override
    public EventBuilder put(String key, String value) {
      return this;
    }

    @Override
    public EventBuilder setTimestamp(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public EventBuilder setContext(Context context) {
      return this;
    }

    @Override
    public EventBuilder setSeverity(Severity severity) {
      return this;
    }

    @Override
    public EventBuilder setAttributes(Attributes attributes) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
