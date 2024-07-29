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

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.funcitonal.Predicate;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A collection of methods that allow use of experimental features prior to availability in public
 * APIs.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class SdkLoggerProviderUtil {

  private SdkLoggerProviderUtil() {}

  /** Reflectively set the {@link ScopeConfigurator} to the {@link SdkLoggerProviderBuilder}. */
  public static void setLoggerConfigurator(
      SdkLoggerProviderBuilder sdkLoggerProviderBuilder,
      ScopeConfigurator<LoggerConfig> loggerConfigurator) {
    try {
      Method method =
          SdkLoggerProviderBuilder.class.getDeclaredMethod(
              "setLoggerConfigurator", ScopeConfigurator.class);
      method.setAccessible(true);
      method.invoke(sdkLoggerProviderBuilder, loggerConfigurator);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling setLoggerConfigurator on SdkLoggerProviderBuilder", e);
    }
  }

  /** Reflectively add a logger configurator condition to the {@link SdkLoggerProviderBuilder}. */
  public static void addLoggerConfiguratorCondition(
      SdkLoggerProviderBuilder sdkLoggerProviderBuilder,
      Predicate<InstrumentationLibraryInfo> scopeMatcher,
      LoggerConfig loggerConfig) {
    try {
      Method method =
          SdkLoggerProviderBuilder.class.getDeclaredMethod(
              "addLoggerConfiguratorCondition", Predicate.class, LoggerConfig.class);
      method.setAccessible(true);
      method.invoke(sdkLoggerProviderBuilder, scopeMatcher, loggerConfig);
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      throw new IllegalStateException(
          "Error calling addLoggerConfiguratorCondition on SdkLoggerProviderBuilder", e);
    }
  }
}
