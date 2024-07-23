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
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.logs.v1.LogRecord;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.proto.logs.v1.ScopeLogs;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.extensions.otproto.TraceProtoUtils;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class LogAdapter {
  private LogAdapter() {}

  static List<ResourceLogs> toProtoResourceLogs(Collection<LogRecordData> logs) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>> groups =
        groupByResourceAndLibrary(logs);
    List<ResourceLogs> resourceLogs = new ArrayList<>(groups.size());
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>> entryResource :
        groups.entrySet()) {
      List<ScopeLogs> scopeLogs = new ArrayList<>(entryResource.getValue().size());
      for (Map.Entry<InstrumentationLibraryInfo, List<LogRecord>> entryLibrary :
          entryResource.getValue().entrySet()) {
        scopeLogs.add(
            ScopeLogs.newBuilder()
                .setScope(CommonAdapter.toProtoInstrumentationLibrary(entryLibrary.getKey()))
                .addAllLogRecords(entryLibrary.getValue())
                .build());
      }
      resourceLogs.add(
          ResourceLogs.newBuilder()
              .setResource(ResourceAdapter.toProtoResource(entryResource.getKey()))
              .addAllScopeLogs(scopeLogs)
              .build());
    }
    return resourceLogs;
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>>
      groupByResourceAndLibrary(Collection<LogRecordData> logs) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<LogRecord>>> result = new HashMap<>();
    for (LogRecordData logData : logs) {
      Resource resource = logData.getResource();
      Map<InstrumentationLibraryInfo, List<LogRecord>> libraryInfoListMap =
          result.get(logData.getResource());
      if (libraryInfoListMap == null) {
        libraryInfoListMap = new HashMap<>();
        result.put(resource, libraryInfoListMap);
      }
      List<LogRecord> logList = libraryInfoListMap.get(logData.getInstrumentationScopeInfo());
      if (logList == null) {
        logList = new ArrayList<>();
        libraryInfoListMap.put(logData.getInstrumentationScopeInfo(), logList);
      }
      logList.add(toProtoLog(logData));
    }
    return result;
  }

  private static LogRecord toProtoLog(LogRecordData logData) {
    final LogRecord.Builder builder = LogRecord.newBuilder();
    builder.setTimeUnixNano(logData.getTimestampEpochNanos());
    builder.setObservedTimeUnixNano(logData.getObservedTimestampEpochNanos());
    builder.setTraceId(TraceProtoUtils.toProtoTraceId(logData.getSpanContext().getTraceId()));
    builder.setSpanId(TraceProtoUtils.toProtoSpanId(logData.getSpanContext().getSpanId()));
    builder.setSeverityNumberValue(logData.getSeverity().getSeverityNumber());
    builder.setSeverityText(logData.getSeverityText());
    builder.setBody(AnyValue.newBuilder().setStringValue(logData.getBody().asString()).build());
    logData
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
