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
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.IOException;

final class NoopLogRecordProcessor implements LogRecordProcessor {
  private static final NoopLogRecordProcessor INSTANCE = new NoopLogRecordProcessor();

  static LogRecordProcessor getInstance() {
    return INSTANCE;
  }

  private NoopLogRecordProcessor() {}

  @Override
  public void onEmit(Context context, ReadWriteLogRecord logRecord) {}

  @Override
  public CompletableResultCode shutdown() {
    return forceFlush();
  }

  @Override
  public CompletableResultCode forceFlush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    return "NoopLogRecordProcessor";
  }

  @Override
  public void close() throws IOException {}
}
