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

package io.opentelemetry.sdk.logs;

import io.opentelemetry.logs.LogRecordBuilder;
import io.opentelemetry.logs.LogRecordDataProvider;
import io.opentelemetry.logs.Logger;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;

/** SDK implementation of {@link Logger}. */
final class SdkLogger implements Logger {

  private static final Logger NOOP_LOGGER =
      LogRecordDataProvider.LoggerProviderHelper.noop().get("noop");

  private final LoggerSharedState loggerSharedState;
  private final InstrumentationLibraryInfo instrumentationScopeInfo;
  private final LoggerConfig loggerConfig;

  SdkLogger(
      LoggerSharedState loggerSharedState,
      InstrumentationLibraryInfo instrumentationScopeInfo,
      LoggerConfig loggerConfig) {
    this.loggerSharedState = loggerSharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.loggerConfig = loggerConfig;
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    if (loggerConfig.isEnabled()) {
      return new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo);
    }
    return NOOP_LOGGER.logRecordBuilder();
  }

  // VisibleForTesting
  InstrumentationLibraryInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }
}
