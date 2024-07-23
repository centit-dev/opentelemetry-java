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

public interface EventLoggerBuilder {

  /**
   * Set the scope schema URL of the resulting {@link EventLogger}. Schema URL is part of {@link
   * EventLogger} identity.
   *
   * @param schemaUrl The schema URL.
   * @return this
   */
  EventLoggerBuilder setSchemaUrl(String schemaUrl);

  /**
   * Sets the instrumentation scope version of the resulting {@link EventLogger}. Version is part of
   * {@link EventLogger} identity.
   *
   * @param instrumentationScopeVersion The instrumentation scope version.
   * @return this
   */
  EventLoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link EventLogger} instance.
   *
   * @return a {@link EventLogger} instance configured with the provided options.
   */
  EventLogger build();
}
