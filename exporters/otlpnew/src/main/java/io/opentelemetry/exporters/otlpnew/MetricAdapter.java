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

import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;

import io.opentelemetry.common.Labels;
import io.opentelemetry.common.ReadableKeyValuePairs;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.metrics.v1.Summary;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MetricAdapter {
  private MetricAdapter() {}

  static List<ResourceMetrics> toProtoResourceMetrics(Collection<MetricData> metricData) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Metric>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(metricData);
    List<ResourceMetrics> resourceMetrics = new ArrayList<>(resourceAndLibraryMap.size());
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<Metric>>> entryResource :
        resourceAndLibraryMap.entrySet()) {
      List<ScopeMetrics> instrumentationLibraryMetrics =
          new ArrayList<>(entryResource.getValue().size());
      for (Map.Entry<InstrumentationLibraryInfo, List<Metric>> entryLibrary :
          entryResource.getValue().entrySet()) {
        instrumentationLibraryMetrics.add(
            ScopeMetrics.newBuilder()
                .setScope(CommonAdapter.toProtoInstrumentationLibrary(entryLibrary.getKey()))
                .addAllMetrics(entryLibrary.getValue())
                .build());
      }
      resourceMetrics.add(
          ResourceMetrics.newBuilder()
              .setResource(ResourceAdapter.toProtoResource(entryResource.getKey()))
              .addAllScopeMetrics(instrumentationLibraryMetrics)
              .build());
    }
    return resourceMetrics;
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<Metric>>>
      groupByResourceAndLibrary(Collection<MetricData> metricDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Metric>>> result = new HashMap<>();
    for (MetricData metricData : metricDataList) {
      Resource resource = metricData.getResource();
      Map<InstrumentationLibraryInfo, List<Metric>> libraryInfoListMap =
          result.get(metricData.getResource());
      if (libraryInfoListMap == null) {
        libraryInfoListMap = new HashMap<>();
        result.put(resource, libraryInfoListMap);
      }
      List<Metric> metricList = libraryInfoListMap.get(metricData.getInstrumentationLibraryInfo());
      if (metricList == null) {
        metricList = new ArrayList<>();
        libraryInfoListMap.put(metricData.getInstrumentationLibraryInfo(), metricList);
      }
      metricList.add(toProtoMetric(metricData));
    }
    return result;
  }

  // fall through comment isn't working for some reason.
  @SuppressWarnings("fallthrough")
  static Metric toProtoMetric(MetricData metricData) {
    MetricData.Descriptor descriptor = metricData.getDescriptor();
    Metric.Builder builder =
        Metric.newBuilder()
            .setName(descriptor.getName())
            .setDescription(descriptor.getDescription())
            .setUnit(descriptor.getUnit());

    // If no points available then return.
    if (metricData.getPoints().isEmpty()) {
      return builder.build();
    }

    boolean monotonic = false;

    switch (descriptor.getType()) {
      case MONOTONIC_LONG:
        monotonic = true;
        // fall through
      case NON_MONOTONIC_LONG:
        builder.setSum(
            Sum.newBuilder()
                .setIsMonotonic(monotonic)
                .setAggregationTemporality(mapToTemporality(descriptor))
                .addAllDataPoints(
                    toIntDataPoints(metricData.getPoints(), metricData.getDescriptor()))
                .build());
        break;
      case MONOTONIC_DOUBLE:
        monotonic = true;
        // fall through
      case NON_MONOTONIC_DOUBLE:
        builder.setSum(
            Sum.newBuilder()
                .setIsMonotonic(monotonic)
                .setAggregationTemporality(mapToTemporality(descriptor))
                .addAllDataPoints(
                    toDoubleDataPoints(metricData.getPoints(), metricData.getDescriptor()))
                .build());
        break;
      case SUMMARY:
        builder.setSummary(
            Summary.newBuilder()
                .addAllDataPoints(
                    toSummaryDataPoints(metricData.getPoints(), metricData.getDescriptor()))
                .build());
        break;
    }
    return builder.build();
  }

  private static AggregationTemporality mapToTemporality(MetricData.Descriptor descriptor) {
    switch (descriptor.getType()) {
      case NON_MONOTONIC_LONG:
      case NON_MONOTONIC_DOUBLE:
      case MONOTONIC_LONG:
      case MONOTONIC_DOUBLE:
        return AGGREGATION_TEMPORALITY_CUMULATIVE;
      case SUMMARY:
        return AGGREGATION_TEMPORALITY_DELTA;
    }
    return AGGREGATION_TEMPORALITY_UNSPECIFIED;
  }

  static List<NumberDataPoint> toIntDataPoints(
      Collection<MetricData.Point> points, MetricData.Descriptor descriptor) {
    List<NumberDataPoint> result = new ArrayList<>(points.size());
    for (MetricData.Point point : points) {
      MetricData.LongPoint longPoint = (MetricData.LongPoint) point;
      NumberDataPoint.Builder builder =
          NumberDataPoint.newBuilder()
              .setStartTimeUnixNano(longPoint.getStartEpochNanos())
              .setTimeUnixNano(longPoint.getEpochNanos())
              .setAsInt(longPoint.getValue());
      // Avoid calling addAllLabels when not needed to save a couple allocations.
      if (descriptor.getConstantLabels() != null && !descriptor.getConstantLabels().isEmpty()) {
        builder.addAllAttributes(toProtoLabels(descriptor.getConstantLabels()));
      }
      Collection<KeyValue> labels = toProtoLabels(longPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllAttributes(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  static Collection<NumberDataPoint> toDoubleDataPoints(
      Collection<MetricData.Point> points, MetricData.Descriptor descriptor) {
    List<NumberDataPoint> result = new ArrayList<>(points.size());
    for (MetricData.Point point : points) {
      MetricData.DoublePoint doublePoint = (MetricData.DoublePoint) point;
      NumberDataPoint.Builder builder =
          NumberDataPoint.newBuilder()
              .setStartTimeUnixNano(doublePoint.getStartEpochNanos())
              .setTimeUnixNano(doublePoint.getEpochNanos())
              .setAsDouble(doublePoint.getValue());
      // Avoid calling addAllLabels when not needed to save a couple allocations.
      if (descriptor.getConstantLabels() != null && !descriptor.getConstantLabels().isEmpty()) {
        builder.addAllAttributes(toProtoLabels(descriptor.getConstantLabels()));
      }
      Collection<KeyValue> labels = toProtoLabels(doublePoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllAttributes(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  static List<SummaryDataPoint> toSummaryDataPoints(
      Collection<MetricData.Point> points, MetricData.Descriptor descriptor) {
    List<SummaryDataPoint> result = new ArrayList<>(points.size());
    for (MetricData.Point point : points) {
      MetricData.SummaryPoint summaryPoint = (MetricData.SummaryPoint) point;
      SummaryDataPoint.Builder builder =
          SummaryDataPoint.newBuilder()
              .setStartTimeUnixNano(summaryPoint.getStartEpochNanos())
              .setTimeUnixNano(summaryPoint.getEpochNanos())
              .setCount(summaryPoint.getCount())
              .setSum(summaryPoint.getSum());
      // Avoid calling addAllLabels when not needed to save a couple allocations.
      if (descriptor.getConstantLabels() != null && !descriptor.getConstantLabels().isEmpty()) {
        builder.addAllAttributes(toProtoLabels(descriptor.getConstantLabels()));
      }
      List<KeyValue> labels = toProtoLabels(summaryPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllAttributes(labels);
      }
      // Not calling directly addAllPercentileValues because that generates couple of unnecessary
      // allocations if empty list.
      if (!summaryPoint.getPercentileValues().isEmpty()) {
        addBucketValues(summaryPoint.getPercentileValues(), builder);
      }
      result.add(builder.build());
    }
    return result;
  }

  // TODO: Consider to pass the Builder and directly add values.
  @SuppressWarnings("MixedMutabilityReturnType")
  static void addBucketValues(
      List<MetricData.ValueAtPercentile> valueAtPercentiles, SummaryDataPoint.Builder builder) {

    for (MetricData.ValueAtPercentile valueAtPercentile : valueAtPercentiles) {
      // TODO(jkwatson): Value of histogram should be long?
      SummaryDataPoint.ValueAtQuantile value =
          SummaryDataPoint.ValueAtQuantile.newBuilder()
              .setQuantile(valueAtPercentile.getPercentile())
              .setValue(valueAtPercentile.getValue())
              .build();
      builder.addQuantileValues(value);
    }

    // No recordings past the highest percentile (e.g., [highest percentile, +infinity]).
  }

  @SuppressWarnings("MixedMutabilityReturnType")
  static List<KeyValue> toProtoLabels(Labels labels) {
    if (labels.isEmpty()) {
      return Collections.emptyList();
    }
    final List<KeyValue> result = new ArrayList<>(labels.size());
    labels.forEach(
        new ReadableKeyValuePairs.KeyValueConsumer<String>() {
          @Override
          public void consume(String key, String value) {
            AnyValue v = AnyValue.newBuilder().setStringValue(value).build();
            result.add(KeyValue.newBuilder().setKey(key).setValue(v).build());
          }
        });
    return result;
  }
}
