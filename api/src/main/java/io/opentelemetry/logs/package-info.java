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

/**
 * API for writing log appenders.
 *
 * <p>The OpenTelemetry logging API exists to enable the creation of log appenders, which bridge
 * logs from other log frameworks (e.g. SLF4J, Log4j, JUL, Logback, etc) into OpenTelemetry via
 * {@link io.opentelemetry.logs.Logger#logRecordBuilder()}. It is <b>NOT</b> a replacement log
 * framework.
 */
@ParametersAreNonnullByDefault
package io.opentelemetry.logs;

import javax.annotation.ParametersAreNonnullByDefault;
