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

public interface EventLoggerProvider {

  /**
   * Gets or creates a named {@link EventLogger} instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a Logger instance.
   */
  EventLogger get(String instrumentationScopeName);

  /**
   * Creates a LoggerBuilder for a named {@link EventLogger} instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a LoggerBuilder instance.
   */
  EventLoggerBuilder eventLoggerBuilder(String instrumentationScopeName);

  class EventLoggerProviderHelper {

    public static EventLogger get(EventLoggerProvider provider, String instrumentationScopeName) {
      return provider.eventLoggerBuilder(instrumentationScopeName).build();
    }

    /**
     * Returns a no-op {@link EventLoggerProvider} which provides Loggers which do not record or
     * emit.
     */
    public static EventLoggerProvider noop() {
      return DefaultEventLoggerProvider.getInstance();
    }
  }
}
