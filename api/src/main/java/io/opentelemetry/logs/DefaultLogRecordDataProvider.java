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

package io.opentelemetry.logs;

class DefaultLogRecordDataProvider implements LogRecordDataProvider {

  private static final LogRecordDataProvider INSTANCE = new DefaultLogRecordDataProvider();
  private static final LoggerBuilder NOOP_BUILDER = new NoopLoggerBuilder();

  private DefaultLogRecordDataProvider() {}

  static LogRecordDataProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public Logger get(String instrumentationScopeName) {
    return LoggerProviderHelper.get(this, instrumentationScopeName);
  }

  @Override
  public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
    return NOOP_BUILDER;
  }

  @Override
  public LogRecordDataProvider noop() {
    return LoggerProviderHelper.noop();
  }

  private static class NoopLoggerBuilder implements LoggerBuilder {

    @Override
    public LoggerBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public LoggerBuilder setInstrumentationVersion(String instrumentationVersion) {
      return this;
    }

    @Override
    public Logger build() {
      return DefaultLogger.getInstance();
    }
  }
}
