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

public interface EventBuilder {

  /** Put the given {@code key} and {@code value} in the payload. */
  EventBuilder put(String key, String value);

  /**
   * Set the epoch {@code timestamp}, using the timestamp and unit.
   *
   * <p>The {@code timestamp} is the time at which the event occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  EventBuilder setTimestamp(long timestamp, TimeUnit unit);

  /** Set the context. */
  EventBuilder setContext(Context context);

  /** Set the severity. */
  EventBuilder setSeverity(Severity severity);

  /**
   * Set the attributes.
   *
   * <p>Event {@link Attributes} provide additional details about the Event which are not part of
   * the well-defined payload. Setting event attributes is less common than adding entries to the
   * event payload. Most users will want to call one of the {@code #put(String, ?)} methods instead.
   */
  EventBuilder setAttributes(Attributes attributes);

  /** Emit an event. */
  void emit();
}
