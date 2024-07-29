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

/**
 * Builder class for creating {@link Logger} instances.
 *
 * <p>{@link Logger}s are identified by their scope name, version, and schema URL. These identifying
 * fields, along with attributes, combine to form the instrumentation scope, which is attached to
 * all log records produced by the {@link Logger}.
 *
 * @since 1.27.0
 */
public interface LoggerBuilder {

  /**
   * Set the scope schema URL of the resulting {@link Logger}. Schema URL is part of {@link Logger}
   * identity.
   *
   * @param schemaUrl The schema URL.
   * @return this
   */
  LoggerBuilder setSchemaUrl(String schemaUrl);

  /**
   * Sets the instrumentation scope version of the resulting {@link Logger}. Version is part of
   * {@link Logger} identity.
   *
   * @param instrumentationScopeVersion The instrumentation scope version.
   * @return this
   */
  LoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link Logger} instance.
   *
   * @return a {@link Logger} instance configured with the provided options.
   */
  Logger build();
}
