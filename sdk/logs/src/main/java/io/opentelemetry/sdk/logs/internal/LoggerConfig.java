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

import com.google.auto.value.AutoValue;
import io.opentelemetry.logs.Logger;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import javax.annotation.concurrent.Immutable;

/**
 * A collection of configuration options which define the behavior of a {@link Logger}.
 *
 * @see SdkLoggerProviderUtil#setLoggerConfigurator(SdkLoggerProviderBuilder, ScopeConfigurator)
 */
@AutoValue
@Immutable
public abstract class LoggerConfig {

  private static final LoggerConfig DEFAULT_CONFIG =
      new AutoValue_LoggerConfig(/* enabled= */ true);
  private static final LoggerConfig DISABLED_CONFIG =
      new AutoValue_LoggerConfig(/* enabled= */ false);

  /** Returns a disabled {@link LoggerConfig}. */
  public static LoggerConfig disabled() {
    return DISABLED_CONFIG;
  }

  /** Returns an enabled {@link LoggerConfig}. */
  public static LoggerConfig enabled() {
    return DEFAULT_CONFIG;
  }

  /**
   * Returns the default {@link LoggerConfig}, which is used when no configurator is set or when the
   * logger configurator returns {@code null} for a {@link InstrumentationLibraryInfo}.
   */
  public static LoggerConfig defaultConfig() {
    return DEFAULT_CONFIG;
  }

  /**
   * Create a {@link ScopeConfiguratorBuilder} for configuring {@link
   * SdkLoggerProviderUtil#setLoggerConfigurator(SdkLoggerProviderBuilder, ScopeConfigurator)}.
   */
  public static ScopeConfiguratorBuilder<LoggerConfig> configuratorBuilder() {
    return ScopeConfigurator.ScopeConfiguratorHelper.builder();
  }

  LoggerConfig() {}

  /** Returns {@code true} if this logger is enabled. Defaults to {@code true}. */
  public abstract boolean isEnabled();
}
