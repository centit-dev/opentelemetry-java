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

package io.opentelemetry.sdk.extensions.otproto;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.protobuf.ByteString;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceId;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceProtoUtils}. */
class TraceProtoUtilsTest {

  private static final byte[] TRACE_ID_BYTES =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final TraceId TRACE_ID = TraceId.fromBytes(TRACE_ID_BYTES, 0);
  private static final byte[] SPAN_ID_BYTES = new byte[] {0, 0, 0, 0, 0, 0, 0, 'b'};
  private static final SpanId SPAN_ID = SpanId.fromBytes(SPAN_ID_BYTES, 0);

  @Test
  void toProtoTraceId() {
    ByteString expected = ByteString.copyFrom(TRACE_ID_BYTES);
    assertThat(TraceProtoUtils.toProtoTraceId(TRACE_ID)).isEqualTo(expected);
  }

  @Test
  void toProtoSpanId() {
    ByteString expected = ByteString.copyFrom(SPAN_ID_BYTES);
    assertThat(TraceProtoUtils.toProtoSpanId(SPAN_ID)).isEqualTo(expected);
  }

}
