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
 * A {@link Logger} is the entry point into a log pipeline.
 *
 * <p>Obtain a {@link #logRecordBuilder()}, add properties using the setters, and emit it via {@link
 * LogRecordBuilder#emit()}.
 *
 * <p>The OpenTelemetry logs bridge API exists to enable bridging logs from other log frameworks
 * (e.g. SLF4J, Log4j, JUL, Logback, etc) into OpenTelemetry and is <b>NOT</b> a replacement log
 * API.
 *
 * @since 1.27.0
 */
@ThreadSafe
public interface Logger {

  /**
   * Return a {@link LogRecordBuilder} to emit a log record.
   *
   * <p><b>IMPORTANT:</b> this should be used to write appenders to bridge logs from logging
   * frameworks (e.g. SLF4J, Log4j, JUL, Logback, etc). It is <b>NOT</b> a replacement for an
   * application logging framework, and should not be used by application developers.
   *
   * <p>Build the log record using the {@link LogRecordBuilder} setters, and emit via {@link
   * LogRecordBuilder#emit()}.
   */
  LogRecordBuilder logRecordBuilder();
}
