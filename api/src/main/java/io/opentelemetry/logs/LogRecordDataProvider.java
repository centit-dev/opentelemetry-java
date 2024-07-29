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

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating scoped {@link Logger}s. The name <i>Provider</i> is for consistency with
 * other languages and it is <b>NOT</b> loaded using reflection.
 *
 * <p>The OpenTelemetry logs bridge API exists to enable bridging logs from other log frameworks
 * (e.g. SLF4J, Log4j, JUL, Logback, etc) into OpenTelemetry and is <b>NOT</b> a replacement log
 * API.
 *
 * @since 1.27.0
 * @see Logger
 */
@ThreadSafe
public interface LogRecordDataProvider {

  /**
   * Gets or creates a named Logger instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a Logger instance.
   */
  Logger get(String instrumentationScopeName);

  /**
   * Creates a LoggerBuilder for a named Logger instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a LoggerBuilder instance.
   */
  LoggerBuilder loggerBuilder(String instrumentationScopeName);

  /**
   * Returns a no-op {@link LogRecordDataProvider}
   * which provides Loggers which do not record or emit.
   */
  LogRecordDataProvider noop();

  class LoggerProviderHelper {
    public static Logger get(LogRecordDataProvider provider, String instrumentationScopeName) {
      return provider.loggerBuilder(instrumentationScopeName).build();
    }

    public static LogRecordDataProvider noop() {
      return DefaultLogRecordDataProvider.getInstance();
    }
  }
}
