package io.vertx.dgraph;

import io.dgraph.DgraphGrpc;
import io.dgraph.DgraphProto;
import io.grpc.ManagedChannel;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.grpc.VertxChannelBuilder;

import java.net.InetSocketAddress;

public class DGraphClient {

    private DgraphGrpc.DgraphVertxStub stub;

    public DGraphClient(Vertx vertx) {
        ManagedChannel channel = VertxChannelBuilder
                .forAddress(vertx, new InetSocketAddress("localhost", 9080))
                .usePlaintext(true)
                .build();
        stub = DgraphGrpc.newVertxStub(channel);

    }

    public void alter(DgraphProto.Operation request, Handler<AsyncResult<DgraphProto.Payload>> resultHandler) {
        stub.alter(request, resultHandler);
    }

    public void checkVersion(DgraphProto.Check request, Handler<AsyncResult<DgraphProto.Version>> resultHandler) {
        stub.checkVersion(request, resultHandler);
    }

    public void commitOrAbort(DgraphProto.TxnContext request, Handler<AsyncResult<DgraphProto.TxnContext>> resultHandler) {
        stub.commitOrAbort(request, resultHandler);
    }

    public void mutate(DgraphProto.Mutation request, Handler<AsyncResult<DgraphProto.Assigned>> resultHandler) {
        stub.mutate(request, resultHandler);
    }

    public void query(DgraphProto.Request request, Handler<AsyncResult<DgraphProto.Response>> resultHandler) {
        stub.query(request, resultHandler);
    }

}
