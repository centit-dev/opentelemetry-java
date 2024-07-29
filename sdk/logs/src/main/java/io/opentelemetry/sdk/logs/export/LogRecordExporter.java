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

package io.opentelemetry.sdk.logs.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.LogRecordProcessor;
import io.opentelemetry.sdk.logs.SdkLogRecordDataProvider;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * An exporter is responsible for taking a collection of {@link LogRecordData}s and transmitting
 * them to their ultimate destination.
 *
 * @since 1.27.0
 */
public interface LogRecordExporter extends Closeable {

  /**
   * Exports the collections of given {@link LogRecordData}.
   *
   * @param logs the collection of {@link LogRecordData} to be exported
   * @return the result of the export, which is often an asynchronous operation
   */
  CompletableResultCode export(Collection<LogRecordData> logs);

  /**
   * Exports the collection of {@link LogRecordData} that have not yet been exported.
   *
   * @return the result of the flush, which is often an asynchronous operation
   */
  CompletableResultCode flush();

  /**
   * Shutdown the log exporter. Called when {@link SdkLogRecordDataProvider#shutdown()}
   * is called when this exporter is registered to the provider via {@link BatchLogRecordProcessor}
   * or {@link SimpleLogRecordProcessor}.
   *
   * @return a {@link CompletableResultCode} which is completed when shutdown completes
   */
  CompletableResultCode shutdown();

  class LogRecordExporterHelper {

    /**
     * Returns a {@link LogRecordExporter} which delegates all exports to the {@code exporters} in
     * order.
     *
     * <p>Can be used to export to multiple backends using the same {@link LogRecordProcessor} like
     * a {@link SimpleLogRecordProcessor} or a {@link BatchLogRecordProcessor}.
     */
    public static LogRecordExporter composite(LogRecordExporter... exporters) {
      return composite(Arrays.asList(exporters));
    }

    /**
     * Returns a {@link LogRecordExporter} which delegates all exports to the {@code exporters} in
     * order.
     *
     * <p>Can be used to export to multiple backends using the same {@link LogRecordProcessor} like
     * a {@link SimpleLogRecordProcessor} or a {@link BatchLogRecordProcessor}.
     */
    public static LogRecordExporter composite(Iterable<LogRecordExporter> exporters) {
      List<LogRecordExporter> exportersList = new ArrayList<>();
      for (LogRecordExporter exporter : exporters) {
        exportersList.add(exporter);
      }
      if (exportersList.isEmpty()) {
        return NoopLogRecordExporter.getInstance();
      }
      if (exportersList.size() == 1) {
        return exportersList.get(0);
      }
      return MultiLogRecordExporter.create(exportersList);
    }

    public static void close(LogRecordExporter exporter) {
      exporter.shutdown().join(10, TimeUnit.SECONDS);
    }
  }
}
