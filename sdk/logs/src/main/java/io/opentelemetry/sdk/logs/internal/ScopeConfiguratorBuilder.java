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
import io.opentelemetry.sdk.logs.funcitonal.Predicate;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public final class ScopeConfiguratorBuilder<T> {

  private final ScopeConfigurator<T> baseScopeConfigurator;
  @Nullable private T defaultScopeConfig;
  private final List<Condition<T>> conditions = new ArrayList<>();

  ScopeConfiguratorBuilder(ScopeConfigurator<T> baseScopeConfigurator) {
    this.baseScopeConfigurator = baseScopeConfigurator;
  }

  /**
   * Set the default scope config, which is returned by {@link ScopeConfigurator#apply(Object)} if a
   * {@link InstrumentationLibraryInfo} does not match any {@link #addCondition(Predicate, Object)
   * conditions}. If a default is not set, an SDK defined default is used.
   */
  public ScopeConfiguratorBuilder<T> setDefault(T defaultScopeConfig) {
    this.defaultScopeConfig = defaultScopeConfig;
    return this;
  }

  /**
   * Add a condition. Conditions are evaluated in order. The {@code scopeConfig} for the first match
   * is returned by {@link ScopeConfigurator#apply(Object)}.
   *
   * @param scopePredicate predicate that {@link InstrumentationLibraryInfo}s are evaluated against
   * @param scopeConfig the scope config to use when this condition is the first matching {@code
   *     scopePredicate}
   * @see #nameMatchesGlob(String)
   * @see #nameEquals(String)
   */
  public ScopeConfiguratorBuilder<T> addCondition(
      Predicate<InstrumentationLibraryInfo> scopePredicate, T scopeConfig) {
    conditions.add(new Condition<>(scopePredicate, scopeConfig));
    return this;
  }

  /**
   * Helper function for pattern matching {@link InstrumentationLibraryInfo#getName()} against the
   * {@code globPattern}.
   *
   * <p>{@code globPattern} may contain the wildcard characters {@code *} and {@code ?} with the
   * following matching criteria:
   *
   * <ul>
   *   <li>{@code *} matches 0 or more instances of any character
   *   <li>{@code ?} matches exactly one instance of any character
   * </ul>
   *
   * @see #addCondition(Predicate, Object)
   */
  public static Predicate<InstrumentationLibraryInfo> nameMatchesGlob(String globPattern) {
    final Predicate<String> globPredicate = GlobUtil.toGlobPatternPredicate(globPattern);
    return new Predicate<InstrumentationLibraryInfo>() {
      @Override
      public boolean test(InstrumentationLibraryInfo instrumentationLibraryInfo) {
        return globPredicate.test(instrumentationLibraryInfo.getName());
      }
    };
  }

  /**
   * Helper function for exact matching {@link InstrumentationLibraryInfo#getName()} against the
   * {@code scopeName}.
   *
   * @see #addCondition(Predicate, Object)
   */
  public static Predicate<InstrumentationLibraryInfo> nameEquals(final String scopeName) {
    return new Predicate<InstrumentationLibraryInfo>() {
      @Override
      public boolean test(InstrumentationLibraryInfo instrumentationLibraryInfo) {
        return instrumentationLibraryInfo.getName().equals(scopeName);
      }
    };
  }

  /** Build a {@link ScopeConfigurator} with the configuration of this builder. */
  public ScopeConfigurator<T> build() {
    // TODO: return an instance with toString implementation which self describes rules
    return new ScopeConfigurator<T>() {
      @Override
      public T apply(InstrumentationLibraryInfo scopeInfo) {
        T scopeConfig = baseScopeConfigurator.apply(scopeInfo);
        if (scopeConfig != null) {
          return scopeConfig;
        }
        for (Condition<T> condition : conditions) {
          if (condition.scopeMatcher.test(scopeInfo)) {
            return condition.scopeConfig;
          }
        }
        return defaultScopeConfig;
      }
    };
  }

  private static final class Condition<T> {
    private final Predicate<InstrumentationLibraryInfo> scopeMatcher;
    private final T scopeConfig;

    private Condition(Predicate<InstrumentationLibraryInfo> scopeMatcher, T scopeConfig) {
      this.scopeMatcher = scopeMatcher;
      this.scopeConfig = scopeConfig;
    }
  }
}
