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
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.logs.LogRecordBuilder;
import io.opentelemetry.logs.Severity;
import io.opentelemetry.sdk.common.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class SdkEventBuilder implements EventBuilder {

  private static final String EVENT_NAME = "event.name";

  private final Map<String, String> payload = new HashMap<>();
  private final Clock clock;
  private final LogRecordBuilder logRecordBuilder;
  private final String eventName;
  private boolean hasTimestamp = false;

  SdkEventBuilder(Clock clock, LogRecordBuilder logRecordBuilder, String eventName) {
    this.clock = clock;
    this.logRecordBuilder = logRecordBuilder;
    this.eventName = eventName;
  }

  @Override
  public EventBuilder put(String key, String value) {
    payload.put(key, value);
    return this;
  }

  @Override
  public EventBuilder setTimestamp(long timestamp, TimeUnit unit) {
    this.logRecordBuilder.setTimestamp(timestamp, unit);
    this.hasTimestamp = true;
    return this;
  }

  @Override
  public EventBuilder setContext(Context context) {
    logRecordBuilder.setContext(context);
    return this;
  }

  @Override
  public EventBuilder setSeverity(Severity severity) {
    logRecordBuilder.setSeverity(severity);
    return this;
  }

  @Override
  public EventBuilder setAttributes(Attributes attributes) {
    logRecordBuilder.setAllAttributes(attributes);
    return this;
  }

  @Override
  public void emit() {
    if (!payload.isEmpty()) {
      Attributes.Builder builder = Attributes.newBuilder();
      for (Map.Entry<String, String> entry : payload.entrySet()) {
        builder.setAttribute(entry.getKey(), entry.getValue());
      }
      logRecordBuilder.setAllAttributes(builder.build());
    }
    if (!hasTimestamp) {
      logRecordBuilder.setTimestamp(clock.now(), TimeUnit.NANOSECONDS);
    }
    logRecordBuilder.setAttribute(EVENT_NAME, AttributeValue.stringAttributeValue(eventName));
    logRecordBuilder.emit();
  }
}
