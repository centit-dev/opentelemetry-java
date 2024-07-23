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

package io.opentelemetry.sdk.logs;

import io.grpc.Context;
import io.opentelemetry.logs.LogRecordBuilder;
import io.opentelemetry.logs.Logger;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/**
 * {@link LogRecordProcessor} is the interface to allow synchronous hooks for log records emitted by
 * {@link Logger}s.
 *
 * @since 1.27.0
 */
@ThreadSafe
public interface LogRecordProcessor extends Closeable {

  /**
   * Called when a {@link Logger} {@link LogRecordBuilder#emit()}s a log record.
   *
   * @param context the context set via {@link LogRecordBuilder#setContext(Context)}, or {@link
   *     Context#current()} if not explicitly set
   * @param logRecord the log record
   */
  void onEmit(Context context, ReadWriteLogRecord logRecord);

  /**
   * Shutdown the log processor.
   *
   * @return result
   */
  CompletableResultCode shutdown();

  /**
   * Process all log records that have not yet been processed.
   *
   * @return result
   */
  CompletableResultCode forceFlush();

  class LogRecordProcessorHelper {
    public static LogRecordProcessor composite(LogRecordProcessor... processors) {
      return LogRecordProcessorHelper.composite(Arrays.asList(processors));
    }

    public static LogRecordProcessor composite(Iterable<LogRecordProcessor> processors) {
      List<LogRecordProcessor> processorList = new ArrayList<>();
      for (LogRecordProcessor processor : processors) {
        processorList.add(processor);
      }
      if (processorList.isEmpty()) {
        return NoopLogRecordProcessor.getInstance();
      }
      if (processorList.size() == 1) {
        return processorList.get(0);
      }
      return MultiLogRecordProcessor.create(processorList);
    }

    /**
     * Shutdown the log processor.
     *
     * @return result
     */
    public static CompletableResultCode shutdown(LogRecordProcessor processor) {
      return processor.forceFlush();
    }

    /**
     * Process all log records that have not yet been processed.
     *
     * @return result
     */
    public static CompletableResultCode forceFlush(LogRecordProcessor processor) {
      return CompletableResultCode.ofSuccess();
    }

    /**
     * Closes this {@link LogRecordProcessor} after processing any remaining log records, releasing
     * any resources.
     */
    public static void close(LogRecordProcessor processor) {
      processor.shutdown().join(10, TimeUnit.SECONDS);
    }
  }
}
