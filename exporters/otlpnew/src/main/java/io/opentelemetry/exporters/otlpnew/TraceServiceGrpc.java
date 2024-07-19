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

import io.grpc.stub.StreamObserver;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;

/**
 *
 *
 * <pre>
 * Service that can be used to push spans between one Application instrumented with
 * OpenTelemetry and a collector, or between a collector and a central collector (in this
 * case spans are sent/received to/from multiple Applications).
 * </pre>
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.56.1)",
    comments = "Source: opentelemetry/proto/collector/trace/v1/trace_service.proto")
public final class TraceServiceGrpc {

  public static final String SERVICE_NAME = "opentelemetry.proto.collector.trace.v1.TraceService";
  private static final int METHODID_EXPORT = 0;
  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<
          io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest,
          io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse>
      getExportMethod;
  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  private TraceServiceGrpc() {}

  /** something. */
  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Export",
      requestType = io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest.class,
      responseType = io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<
          io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest,
          io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse>
      getExportMethod() {
    io.grpc.MethodDescriptor<
            io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest,
            io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse>
        getExportMethod;
    if ((getExportMethod = TraceServiceGrpc.getExportMethod) == null) {
      synchronized (TraceServiceGrpc.class) {
        if ((getExportMethod = TraceServiceGrpc.getExportMethod) == null) {
          TraceServiceGrpc.getExportMethod =
              getExportMethod =
                  io.grpc.MethodDescriptor
                      .<io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest,
                          io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse>
                          newBuilder()
                      .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
                      .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Export"))
                      .setSampledToLocalTracing(true)
                      .setRequestMarshaller(
                          io.grpc.protobuf.ProtoUtils.marshaller(
                              io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest
                                  .getDefaultInstance()))
                      .setResponseMarshaller(
                          io.grpc.protobuf.ProtoUtils.marshaller(
                              io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse
                                  .getDefaultInstance()))
                      .setSchemaDescriptor(new TraceServiceMethodDescriptorSupplier("Export"))
                      .build();
        }
      }
    }
    return getExportMethod;
  }

  /** Creates a new async stub that supports all call types for the service. */
  public static TraceServiceStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TraceServiceStub> factory =
        new io.grpc.stub.AbstractStub.StubFactory<TraceServiceStub>() {
          @java.lang.Override
          public TraceServiceStub newStub(
              io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TraceServiceStub(channel, callOptions);
          }
        };
    return TraceServiceStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the
   * service.
   */
  public static TraceServiceBlockingStub newBlockingStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TraceServiceBlockingStub> factory =
        new io.grpc.stub.AbstractStub.StubFactory<TraceServiceBlockingStub>() {
          @java.lang.Override
          public TraceServiceBlockingStub newStub(
              io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TraceServiceBlockingStub(channel, callOptions);
          }
        };
    return TraceServiceBlockingStub.newStub(factory, channel);
  }

  /** Creates a new ListenableFuture-style stub that supports unary calls on the service. */
  public static TraceServiceFutureStub newFutureStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TraceServiceFutureStub> factory =
        new io.grpc.stub.AbstractStub.StubFactory<TraceServiceFutureStub>() {
          @java.lang.Override
          public TraceServiceFutureStub newStub(
              io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new TraceServiceFutureStub(channel, callOptions);
          }
        };
    return TraceServiceFutureStub.newStub(factory, channel);
  }

  /** something. */
  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
            getExportMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
                new MethodHandlers<
                    io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest,
                    io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse>(
                    service, METHODID_EXPORT)))
        .build();
  }

  /** something. */
  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (TraceServiceGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor =
              result =
                  io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
                      .setSchemaDescriptor(new TraceServiceFileDescriptorSupplier())
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
   * Service that can be used to push spans between one Application instrumented with
   * OpenTelemetry and a collector, or between a collector and a central collector (in this
   * case spans are sent/received to/from multiple Applications).
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
        io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest request,
        io.grpc.stub.StreamObserver<
                io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse>
            responseObserver);
  }

  /**
   * Base class for the server implementation of the service TraceService.
   *
   * <pre>
   * Service that can be used to push spans between one Application instrumented with
   * OpenTelemetry and a collector, or between a collector and a central collector (in this
   * case spans are sent/received to/from multiple Applications).
   * </pre>
   */
  public abstract static class TraceServiceImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override
    public final io.grpc.ServerServiceDefinition bindService() {
      return TraceServiceGrpc.bindService(this);
    }

    @Override
    public void export(
        ExportTraceServiceRequest request,
        StreamObserver<ExportTraceServiceResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getExportMethod(), responseObserver);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service TraceService.
   *
   * <pre>
   * Service that can be used to push spans between one Application instrumented with
   * OpenTelemetry and a collector, or between a collector and a central collector (in this
   * case spans are sent/received to/from multiple Applications).
   * </pre>
   */
  public static final class TraceServiceStub
      extends io.grpc.stub.AbstractAsyncStub<TraceServiceStub> {
    private TraceServiceStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TraceServiceStub build(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TraceServiceStub(channel, callOptions);
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
        io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest request,
        io.grpc.stub.StreamObserver<
                io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse>
            responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getExportMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service TraceService.
   *
   * <pre>
   * Service that can be used to push spans between one Application instrumented with
   * OpenTelemetry and a collector, or between a collector and a central collector (in this
   * case spans are sent/received to/from multiple Applications).
   * </pre>
   */
  public static final class TraceServiceBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<TraceServiceBlockingStub> {
    private TraceServiceBlockingStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TraceServiceBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TraceServiceBlockingStub(channel, callOptions);
    }

    /**
     *
     *
     * <pre>
     * For performance reasons, it is recommended to keep this RPC
     * alive for the entire life of the application.
     * </pre>
     */
    public io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse export(
        io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getExportMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service TraceService.
   *
   * <pre>
   * Service that can be used to push spans between one Application instrumented with
   * OpenTelemetry and a collector, or between a collector and a central collector (in this
   * case spans are sent/received to/from multiple Applications).
   * </pre>
   */
  public static final class TraceServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<TraceServiceFutureStub> {
    private TraceServiceFutureStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TraceServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TraceServiceFutureStub(channel, callOptions);
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
            io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse>
        export(io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest request) {
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
              (io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest) request,
              (io.grpc.stub.StreamObserver<
                      io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse>)
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

  private abstract static class TraceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier,
          io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TraceServiceBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.opentelemetry.proto.collector.trace.v1.TraceServiceProto.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TraceService");
    }
  }

  private static final class TraceServiceFileDescriptorSupplier
      extends TraceServiceBaseDescriptorSupplier {
    TraceServiceFileDescriptorSupplier() {}
  }

  private static final class TraceServiceMethodDescriptorSupplier
      extends TraceServiceBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    TraceServiceMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }
}
