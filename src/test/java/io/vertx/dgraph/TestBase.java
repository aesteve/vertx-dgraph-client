package io.vertx.dgraph;

import io.dgraph.DgraphProto;
import io.vertx.core.Vertx;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.containers.DockerComposeContainer;

import java.io.File;

import static io.vertx.dgraph.data.TestSet.CREATE_SCHEMA;
import static io.vertx.dgraph.data.TestSet.DROP_ALL;
import static io.vertx.dgraph.data.TestSet.SW_DATA;
import static io.vertx.dgraph.data.TestSet.str;

@ExtendWith(VertxExtension.class)
abstract class TestBase {

    private static final File dockerComposeFile = new File(TestBase.class.getClassLoader().getResource("docker-compose.yml").getFile());
    static {
        if (!dockerComposeFile.exists()) {
            throw new RuntimeException("docker-compose.yml not found in classpath, cannot start integration test");
        }
    }
    private static final DockerComposeContainer compose =
            new DockerComposeContainer(
                    dockerComposeFile
            ).withExposedService("server", 9080);

    private static final DGraphClientOptions OPTS =
            new DGraphClientOptions("localhost", 9080)
                    .setUsePlainText(true);


    private Vertx vertx;
    DGraphClient client;

    @BeforeAll
    static void startDgraph() {
        compose.start();
    }

    @AfterAll
    static void stopDgraph() {
        compose.stop();
    }

    @BeforeEach
    void setup(VertxTestContext ctx) {
        vertx = Vertx.vertx();
        client = new DGraphClient(vertx, OPTS);
        client.alter(DROP_ALL, res -> {
            if (res.failed()) {
                ctx.failNow(res.cause());
                return;
            }
            client.alter(CREATE_SCHEMA, createSchemaRes -> {
                if (createSchemaRes.failed()) {
                    ctx.failNow(createSchemaRes.cause());
                    return;
                }
                final DgraphProto.Mutation insertData = DgraphProto.Mutation
                        .newBuilder()
                        .setCommitNow(true)
                        .setSetJson(str(SW_DATA))
                        .build();
                client.mutate(insertData, ctx.completing());
            });
        });
    }

    @AfterEach
    void tearDown(VertxTestContext ctx) {
        if (client == null) {
            ctx.completeNow();
            return;
        }
        client.alter(DROP_ALL, ctx.succeeding(ignored -> {
            vertx.close(ctx.completing());
        }));
    }

}
