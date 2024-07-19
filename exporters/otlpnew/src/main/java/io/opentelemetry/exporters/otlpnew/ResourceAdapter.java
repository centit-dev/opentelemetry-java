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

package io.opentelemetry.exporters.otlpnew;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.ReadableKeyValuePairs;
import io.opentelemetry.proto.resource.v1.Resource;

final class ResourceAdapter {
  private ResourceAdapter() {}

  static Resource toProtoResource(io.opentelemetry.sdk.resources.Resource resource) {
    final Resource.Builder builder = Resource.newBuilder();
    resource
        .getAttributes()
        .forEach(
            new ReadableKeyValuePairs.KeyValueConsumer<AttributeValue>() {
              @Override
              public void consume(String key, AttributeValue value) {
                builder.addAttributes(CommonAdapter.toProtoAttribute(key, value));
              }
            });
    return builder.build();
  }
}
