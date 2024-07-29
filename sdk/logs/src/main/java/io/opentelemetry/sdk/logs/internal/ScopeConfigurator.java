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
import io.opentelemetry.sdk.logs.funcitonal.Function;

public interface ScopeConfigurator<T> extends Function<InstrumentationLibraryInfo, T> {

  class ScopeConfiguratorHelper {
    /** Create a new builder. */
    public static <T> ScopeConfiguratorBuilder<T> builder() {
      return new ScopeConfiguratorBuilder<>(
          new ScopeConfigurator<T>() {
            @Override
            public T apply(InstrumentationLibraryInfo instrumentationLibraryInfo) {
              return null;
            }
          });
    }

    /**
     * Convert this {@link ScopeConfigurator} to a builder. Additional added matchers only apply
     * when {@link #apply(Object)} returns {@code null}. If this configurator contains {@link
     * ScopeConfiguratorBuilder#setDefault(Object)}, additional matchers are never applied.
     */
    public static <T> ScopeConfiguratorBuilder<T> toBuilder(ScopeConfigurator<T> configurator) {
      return new ScopeConfiguratorBuilder<>(configurator);
    }
  }
}
