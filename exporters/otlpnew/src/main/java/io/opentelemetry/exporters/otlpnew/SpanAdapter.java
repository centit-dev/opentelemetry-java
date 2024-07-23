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

import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CLIENT;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_CONSUMER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_INTERNAL;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_PRODUCER;
import static io.opentelemetry.proto.trace.v1.Span.SpanKind.SPAN_KIND_SERVER;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.ReadableKeyValuePairs;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.proto.trace.v1.ScopeSpans;
import io.opentelemetry.proto.trace.v1.Span;
import io.opentelemetry.proto.trace.v1.Status;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.extensions.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class SpanAdapter {
  private SpanAdapter() {}

  static List<ResourceSpans> toProtoResourceSpans(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Span>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(spanDataList);
    List<ResourceSpans> resourceSpans = new ArrayList<>(resourceAndLibraryMap.size());
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<Span>>> entryResource :
        resourceAndLibraryMap.entrySet()) {
      List<ScopeSpans> instrumentationLibrarySpans =
          new ArrayList<>(entryResource.getValue().size());
      for (Map.Entry<InstrumentationLibraryInfo, List<Span>> entryLibrary :
          entryResource.getValue().entrySet()) {
        instrumentationLibrarySpans.add(
            ScopeSpans.newBuilder()
                .setScope(CommonAdapter.toProtoInstrumentationLibrary(entryLibrary.getKey()))
                .addAllSpans(entryLibrary.getValue())
                .build());
      }
      resourceSpans.add(
          ResourceSpans.newBuilder()
              .setResource(ResourceAdapter.toProtoResource(entryResource.getKey()))
              .addAllScopeSpans(instrumentationLibrarySpans)
              .build());
    }
    return resourceSpans;
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<Span>>>
      groupByResourceAndLibrary(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Span>>> result = new HashMap<>();
    for (SpanData spanData : spanDataList) {
      Resource resource = spanData.getResource();
      Map<InstrumentationLibraryInfo, List<Span>> libraryInfoListMap =
          result.get(spanData.getResource());
      if (libraryInfoListMap == null) {
        libraryInfoListMap = new HashMap<>();
        result.put(resource, libraryInfoListMap);
      }
      List<Span> spanList = libraryInfoListMap.get(spanData.getInstrumentationLibraryInfo());
      if (spanList == null) {
        spanList = new ArrayList<>();
        libraryInfoListMap.put(spanData.getInstrumentationLibraryInfo(), spanList);
      }
      spanList.add(toProtoSpan(spanData));
    }
    return result;
  }

  static Span toProtoSpan(SpanData spanData) {
    final Span.Builder builder = Span.newBuilder();
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(spanData.getTraceId()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(spanData.getSpanId()));
    // TODO: Set TraceState;
    if (spanData.getParentSpanId().isValid()) {
      builder.setParentSpanId(TraceProtoUtils.toProtoSpanId(spanData.getParentSpanId()));
    }
    builder.setName(spanData.getName());
    builder.setKind(toProtoSpanKind(spanData.getKind()));
    builder.setStartTimeUnixNano(spanData.getStartEpochNanos());
    builder.setEndTimeUnixNano(spanData.getEndEpochNanos());
    spanData
        .getAttributes()
        .forEach(
            new ReadableKeyValuePairs.KeyValueConsumer<AttributeValue>() {
              @Override
              public void consume(String key, AttributeValue value) {
                builder.addAttributes(CommonAdapter.toProtoAttribute(key, value));
              }
            });
    builder.setDroppedAttributesCount(
        spanData.getTotalAttributeCount() - spanData.getAttributes().size());
    for (SpanData.Event event : spanData.getEvents()) {
      builder.addEvents(toProtoSpanEvent(event));
    }
    builder.setDroppedEventsCount(spanData.getTotalRecordedEvents() - spanData.getEvents().size());
    for (SpanData.Link link : spanData.getLinks()) {
      builder.addLinks(toProtoSpanLink(link));
    }
    builder.setDroppedLinksCount(spanData.getTotalRecordedLinks() - spanData.getLinks().size());
    builder.setStatus(toStatusProto(spanData.getStatus()));
    return builder.build();
  }

  static Span.SpanKind toProtoSpanKind(io.opentelemetry.trace.Span.Kind kind) {
    switch (kind) {
      case INTERNAL:
        return SPAN_KIND_INTERNAL;
      case SERVER:
        return SPAN_KIND_SERVER;
      case CLIENT:
        return SPAN_KIND_CLIENT;
      case PRODUCER:
        return SPAN_KIND_PRODUCER;
      case CONSUMER:
        return SPAN_KIND_CONSUMER;
    }
    return Span.SpanKind.UNRECOGNIZED;
  }

  static Span.Event toProtoSpanEvent(SpanData.Event event) {
    final Span.Event.Builder builder = Span.Event.newBuilder();
    builder.setName(event.getName());
    builder.setTimeUnixNano(event.getEpochNanos());
    event
        .getAttributes()
        .forEach(
            new ReadableKeyValuePairs.KeyValueConsumer<AttributeValue>() {
              @Override
              public void consume(String key, AttributeValue value) {
                builder.addAttributes(CommonAdapter.toProtoAttribute(key, value));
              }
            });
    builder.setDroppedAttributesCount(
        event.getTotalAttributeCount() - event.getAttributes().size());
    return builder.build();
  }

  static Span.Link toProtoSpanLink(SpanData.Link link) {
    final Span.Link.Builder builder = Span.Link.newBuilder();
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(link.getContext().getTraceId()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(link.getContext().getSpanId()));
    // TODO: Set TraceState;
    Attributes attributes = link.getAttributes();
    attributes.forEach(
        new ReadableKeyValuePairs.KeyValueConsumer<AttributeValue>() {
          @Override
          public void consume(String key, AttributeValue value) {
            builder.addAttributes(CommonAdapter.toProtoAttribute(key, value));
          }
        });
    builder.setDroppedAttributesCount(link.getTotalAttributeCount() - attributes.size());
    return builder.build();
  }

  static Status toStatusProto(io.opentelemetry.trace.Status status) {
    Status.Builder builder = Status.newBuilder();
    Status.StatusCode statusCode = Status.StatusCode.forNumber(status.getCanonicalCode().value());
    if (statusCode != null) {
      builder.setCode(statusCode);
    }
    if (status.getDescription() != null) {
      builder.setMessage(status.getDescription());
    }
    return builder.build();
  }
}
