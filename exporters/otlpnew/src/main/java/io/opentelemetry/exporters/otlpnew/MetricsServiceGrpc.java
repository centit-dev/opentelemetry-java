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

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 *
 *
 * <pre>
 * Service that can be used to push metrics between one Application
 * instrumented with OpenTelemetry and a collector, or between a collector and a
 * central collector.
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.56.1)",
    comments = "Source: opentelemetry/proto/collector/metrics/v1/metrics_service.proto")
public final class MetricsServiceGrpc {

  public static final String SERVICE_NAME =
      "opentelemetry.proto.collector.metrics.v1.MetricsService";
  private static final int METHODID_EXPORT = 0;
  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<
          io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest,
          io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>
      getExportMethod;
  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  private MetricsServiceGrpc() {}

  /** something. */
  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Export",
      requestType = io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest.class,
      responseType = io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<
          io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest,
          io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>
      getExportMethod() {
    io.grpc.MethodDescriptor<
            io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest,
            io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>
        getExportMethod;
    if ((getExportMethod = MetricsServiceGrpc.getExportMethod) == null) {
      synchronized (MetricsServiceGrpc.class) {
        if ((getExportMethod = MetricsServiceGrpc.getExportMethod) == null) {
          MetricsServiceGrpc.getExportMethod =
              getExportMethod =
                  io.grpc.MethodDescriptor
                      .<io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest,
                          io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>
                          newBuilder()
                      .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                      .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Export"))
                      .setSampledToLocalTracing(true)
                      .setRequestMarshaller(
                          io.grpc.protobuf.ProtoUtils.marshaller(
                              io.opentelemetry.proto.collector.metrics.v1
                                  .ExportMetricsServiceRequest.getDefaultInstance()))
                      .setResponseMarshaller(
                          io.grpc.protobuf.ProtoUtils.marshaller(
                              io.opentelemetry.proto.collector.metrics.v1
                                  .ExportMetricsServiceResponse.getDefaultInstance()))
                      .setSchemaDescriptor(new MetricsServiceMethodDescriptorSupplier("Export"))
                      .build();
        }
      }
    }
    return getExportMethod;
  }

  /** Creates a new async stub that supports all call types for the service. */
  public static MetricsServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MetricsServiceStub> factory =
        new io.grpc.stub.AbstractStub.StubFactory<MetricsServiceStub>() {
          @java.lang.Override
          public MetricsServiceStub newStub(
              io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new MetricsServiceStub(channel, callOptions);
          }
        };
    return MetricsServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the
   * service.
   */
  public static MetricsServiceBlockingStub newBlockingStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MetricsServiceBlockingStub> factory =
        new io.grpc.stub.AbstractStub.StubFactory<MetricsServiceBlockingStub>() {
          @java.lang.Override
          public MetricsServiceBlockingStub newStub(
              io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new MetricsServiceBlockingStub(channel, callOptions);
          }
        };
    return MetricsServiceBlockingStub.newStub(factory, channel);
  }

  /** Creates a new ListenableFuture-style stub that supports unary calls on the service. */
  public static MetricsServiceFutureStub newFutureStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<MetricsServiceFutureStub> factory =
        new io.grpc.stub.AbstractStub.StubFactory<MetricsServiceFutureStub>() {
          @java.lang.Override
          public MetricsServiceFutureStub newStub(
              io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new MetricsServiceFutureStub(channel, callOptions);
          }
        };
    return MetricsServiceFutureStub.newStub(factory, channel);
  }

  /** something. */
  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
            getExportMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
                new MethodHandlers<
                    io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest,
                    io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>(
                    service, METHODID_EXPORT)))
        .build();
  }

  /** something. */
  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (MetricsServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor =
              result =
                  io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                      .setSchemaDescriptor(new MetricsServiceFileDescriptorSupplier())
                      .addMethod(getExportMethod())
                      .build();
        }
      }
    }
    return result;
  }

  /**
   *
   *
   * <pre>
   * Service that can be used to push metrics between one Application
   * instrumented with OpenTelemetry and a collector, or between a collector and a
   * central collector.
   * </pre>
   */
  public interface AsyncService {

    /**
     *
     *
     * <pre>
     * For performance reasons, it is recommended to keep this RPC
     * alive for the entire life of the application.
     * </pre>
     */
    void export(
        io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest request,
        io.grpc.stub.StreamObserver<
                io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>
            responseObserver);
  }

  /**
   * Base class for the server implementation of the service MetricsService.
   *
   * <pre>
   * Service that can be used to push metrics between one Application
   * instrumented with OpenTelemetry and a collector, or between a collector and a
   * central collector.
   * </pre>
   */
  public abstract static class MetricsServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override
    public final io.grpc.ServerServiceDefinition bindService() {
      return MetricsServiceGrpc.bindService(this);
    }

    @java.lang.Override
    public void export(
        io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest request,
        io.grpc.stub.StreamObserver<
                io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>
            responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExportMethod(), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service MetricsService.
   *
   * <pre>
   * Service that can be used to push metrics between one Application
   * instrumented with OpenTelemetry and a collector, or between a collector and a
   * central collector.
   * </pre>
   */
  public static final class MetricsServiceStub
      extends io.grpc.stub.AbstractAsyncStub<MetricsServiceStub> {
    private MetricsServiceStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MetricsServiceStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MetricsServiceStub(channel, callOptions);
    }

    /**
     *
     *
     * <pre>
     * For performance reasons, it is recommended to keep this RPC
     * alive for the entire life of the application.
     * </pre>
     */
    public void export(
        io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest request,
        io.grpc.stub.StreamObserver<
                io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>
            responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExportMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service MetricsService.
   *
   * <pre>
   * Service that can be used to push metrics between one Application
   * instrumented with OpenTelemetry and a collector, or between a collector and a
   * central collector.
   * </pre>
   */
  public static final class MetricsServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<MetricsServiceBlockingStub> {
    private MetricsServiceBlockingStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MetricsServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MetricsServiceBlockingStub(channel, callOptions);
    }

    /**
     *
     *
     * <pre>
     * For performance reasons, it is recommended to keep this RPC
     * alive for the entire life of the application.
     * </pre>
     */
    public io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse export(
        io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExportMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service MetricsService.
   *
   * <pre>
   * Service that can be used to push metrics between one Application
   * instrumented with OpenTelemetry and a collector, or between a collector and a
   * central collector.
   * </pre>
   */
  public static final class MetricsServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<MetricsServiceFutureStub> {
    private MetricsServiceFutureStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MetricsServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MetricsServiceFutureStub(channel, callOptions);
    }

    /**
     *
     *
     * <pre>
     * For performance reasons, it is recommended to keep this RPC
     * alive for the entire life of the application.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<
            io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>
        export(io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExportMethod(), getCallOptions()), request);
    }
  }

  private static final class MethodHandlers<RQT, RST>
      implements io.grpc.stub.ServerCalls.UnaryMethod<RQT, RST>,
          io.grpc.stub.ServerCalls.ServerStreamingMethod<RQT, RST>,
          io.grpc.stub.ServerCalls.ClientStreamingMethod<RQT, RST>,
          io.grpc.stub.ServerCalls.BidiStreamingMethod<RQT, RST> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(RQT request, io.grpc.stub.StreamObserver<RST> responseObserver) {
      switch (methodId) {
        case METHODID_EXPORT:
          serviceImpl.export(
              (io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest) request,
              (io.grpc.stub.StreamObserver<
                      io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse>)
                  responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<RQT> invoke(
        io.grpc.stub.StreamObserver<RST> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private abstract static class MetricsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier,
          io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    MetricsServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.opentelemetry.proto.collector.metrics.v1.MetricsServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("MetricsService");
    }
  }

  private static final class MetricsServiceFileDescriptorSupplier
      extends MetricsServiceBaseDescriptorSupplier {
    MetricsServiceFileDescriptorSupplier() {}
  }

  private static final class MetricsServiceMethodDescriptorSupplier
      extends MetricsServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    MetricsServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }
}
