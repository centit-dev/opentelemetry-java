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

import com.google.common.base.Splitter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ClientInterceptor;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public final class OtlpGrpcLogExporter implements LogRecordExporter {
  public static final String DEFAULT_ENDPOINT = "localhost:55680";
  public static final long DEFAULT_DEADLINE_MS;
  private static final Logger logger;

  static {
    DEFAULT_DEADLINE_MS = TimeUnit.SECONDS.toMillis(1L);
    logger = Logger.getLogger(OtlpGrpcLogExporter.class.getName());
  }

  private final LogsServiceGrpc.LogsServiceFutureStub logService;
  private final ManagedChannel managedChannel;
  private final long deadlineMs;

  private OtlpGrpcLogExporter(ManagedChannel channel, long deadlineMs) {
    this.managedChannel = channel;
    this.deadlineMs = deadlineMs;
    this.logService = LogsServiceGrpc.newFutureStub(channel);
  }

  public static OtlpGrpcLogExporter.Builder newBuilder() {
    return new OtlpGrpcLogExporter.Builder();
  }

  public static OtlpGrpcLogExporter getDefault() {
    return newBuilder().readEnvironmentVariables().readSystemProperties().build();
  }

  /** something. */
  public CompletableResultCode export(final Collection<LogRecordData> logs) {
    ExportLogsServiceRequest exportLogsServiceRequest =
        ExportLogsServiceRequest.newBuilder()
            .addAllResourceLogs(LogAdapter.toProtoResourceLogs(logs))
            .build();
    final CompletableResultCode result = new CompletableResultCode();
    LogsServiceGrpc.LogsServiceFutureStub exporter;
    if (this.deadlineMs > 0L) {
      exporter = this.logService.withDeadlineAfter(this.deadlineMs, TimeUnit.MILLISECONDS);
    } else {
      exporter = this.logService;
    }

    Futures.addCallback(
        exporter.export(exportLogsServiceRequest),
        new FutureCallback<ExportLogsServiceResponse>() {
          public void onSuccess(@Nullable ExportLogsServiceResponse response) {
            logger.info("Succeeded to export " + logs.size() + " logs");
            result.succeed();
          }

          public void onFailure(Throwable t) {
            OtlpGrpcLogExporter.logger.log(Level.WARNING, "Failed to export logs", t);
            result.fail();
          }
        },
        MoreExecutors.directExecutor());
    return result;
  }

  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /** something. */
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = new CompletableResultCode();
    this.managedChannel.notifyWhenStateChanged(
        ConnectivityState.SHUTDOWN,
        new Runnable() {
          public void run() {
            result.succeed();
          }
        });
    this.managedChannel.shutdown();
    return result;
  }

  @Override
  public void close() throws IOException {
    LogRecordExporterHelper.close(this);
  }

  public static class Builder extends ConfigBuilder<OtlpGrpcLogExporter.Builder> {
    private static final String KEY_LOG_TIMEOUT = "otel.otlp.log.timeout";
    private static final String KEY_ENDPOINT = "otel.otlp.endpoint";
    private static final String KEY_USE_TLS = "otel.otlp.use.tls";
    private static final String KEY_METADATA = "otel.otlp.metadata";
    private ManagedChannel channel;
    private long deadlineMs;
    private String endpoint;
    private boolean useTls;
    @Nullable private Metadata metadata;

    private Builder() {
      this.deadlineMs = OtlpGrpcLogExporter.DEFAULT_DEADLINE_MS;
      this.endpoint = "localhost:55680";
    }

    public OtlpGrpcLogExporter.Builder setChannel(ManagedChannel channel) {
      this.channel = channel;
      return this;
    }

    public OtlpGrpcLogExporter.Builder setDeadlineMs(long deadlineMs) {
      this.deadlineMs = deadlineMs;
      return this;
    }

    public OtlpGrpcLogExporter.Builder setEndpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    public OtlpGrpcLogExporter.Builder setUseTls(boolean useTls) {
      this.useTls = useTls;
      return this;
    }

    /** something. */
    public OtlpGrpcLogExporter.Builder addHeader(String key, String value) {
      if (this.metadata == null) {
        this.metadata = new Metadata();
      }

      this.metadata.put(Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER), value);
      return this;
    }

    /** something. */
    public OtlpGrpcLogExporter build() {
      if (this.channel == null) {
        ManagedChannelBuilder<?> managedChannelBuilder =
            ManagedChannelBuilder.forTarget(this.endpoint);
        if (this.useTls) {
          managedChannelBuilder.useTransportSecurity();
        } else {
          managedChannelBuilder.usePlaintext();
        }

        if (this.metadata != null) {
          managedChannelBuilder.intercept(
              new ClientInterceptor[] {MetadataUtils.newAttachHeadersInterceptor(this.metadata)});
        }

        this.channel = managedChannelBuilder.build();
      }

      return new OtlpGrpcLogExporter(this.channel, this.deadlineMs);
    }

    protected OtlpGrpcLogExporter.Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      Long value = getLongProperty(KEY_LOG_TIMEOUT, configMap);
      if (value != null) {
        this.setDeadlineMs(value);
      }

      String endpointValue = getStringProperty(KEY_ENDPOINT, configMap);
      if (endpointValue != null) {
        this.setEndpoint(endpointValue);
      }

      Boolean useTlsValue = getBooleanProperty(KEY_USE_TLS, configMap);
      if (useTlsValue != null) {
        this.setUseTls(useTlsValue);
      }

      String metadataValue = getStringProperty(KEY_METADATA, configMap);
      if (metadataValue != null) {
        Iterator<String> var7 = Splitter.on(';').split(metadataValue).iterator();

        while (var7.hasNext()) {
          String keyValueString = var7.next();
          List<String> keyValue =
              Splitter.on('=')
                  .limit(2)
                  .trimResults()
                  .omitEmptyStrings()
                  .splitToList(keyValueString);
          if (keyValue.size() == 2) {
            this.addHeader(keyValue.get(0), keyValue.get(1));
          }
        }
      }

      return this;
    }
  }
}
