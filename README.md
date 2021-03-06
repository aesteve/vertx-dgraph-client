## Vert.x DGraph Client

[DGraph](https://dgraph.io) is an open source, scalable, distributed, highly available and fast graph database, designed from ground up to be run in production.

Since it's accessible through gRPC calls, it's accessible using Vert.x gRPC client.

This project provides a simple example of how to build a Vert.x client from a [.proto definition](https://raw.githubusercontent.com/dgraph-io/dgraph4j/master/src/main/proto/api.proto), with Gradle Kotlin DSL and [vertx-grpc](https://vertx.io/docs/vertx-grpc/java/)

It's based on the [official Java client for Dgraph](https://github.com/dgraph-io/dgraph4j).
