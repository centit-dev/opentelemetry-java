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

class DefaultEventLoggerProvider implements EventLoggerProvider {

  private static final EventLoggerProvider INSTANCE = new DefaultEventLoggerProvider();
  private static final EventLoggerBuilder NOOP_EVENT_LOGGER_BUILDER = new NoopEventLoggerBuilder();

  private DefaultEventLoggerProvider() {}

  static EventLoggerProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public EventLogger get(String instrumentationScopeName) {
    return EventLoggerProviderHelper.get(this, instrumentationScopeName);
  }

  @Override
  public EventLoggerBuilder eventLoggerBuilder(String instrumentationScopeName) {
    return NOOP_EVENT_LOGGER_BUILDER;
  }

  private static class NoopEventLoggerBuilder implements EventLoggerBuilder {

    @Override
    public EventLoggerBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public EventLoggerBuilder setInstrumentationVersion(String instrumentationVersion) {
      return this;
    }

    @Override
    public EventLogger build() {
      return DefaultEventLogger.getInstance();
    }
  }
}
