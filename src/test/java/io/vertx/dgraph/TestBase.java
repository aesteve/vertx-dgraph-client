package io.vertx.dgraph;

import com.google.protobuf.ByteString;
import io.dgraph.DgraphProto;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;
import java.util.Date;

@RunWith(VertxUnitRunner.class)
public class TestBase {

    protected Vertx vertx;
    protected DGraphClient client;

    private final static DgraphProto.Operation DROP_ALL = DgraphProto.Operation
            .newBuilder()
            .setDropAll(true)
            .build();

    private final static DgraphProto.Operation CREATE_SCHEMA = DgraphProto.Operation
            .newBuilder()
            .setSchema(
                "name: string @index(term) @upsert .\n" +
                "release_date: datetime @index(year) .\n" +
                "revenue: float .\n" +
                "running_time: int ."
            )
            .build();

    private final static JsonArray SW_DATA = new JsonArray()
            .add(character("Luke Skywalker"))
            .add(character("Princess Leia"))
            .add(character("Han Solo"))
            .add(character("Jarjar Binks"))
            .add(new JsonObject()
                    .put("uid", "_:sw4")
                    .put("name", "Star Wars: Episode IV - A New Hope")
                    .put("release_date", "1977-05-25")
                    .put("revenue", 534000000.0)
                    .put("running_time", 124)
            )
            .add(new JsonObject()
                    .put("uid", "_:sw1")
                    .put("name", "Star Wars: Episode I - The Phantom Menace")
                    .put("release_date", "1999-10-13")
                    .put("revenue", 1027000000.0)
                    .put("running_time", 136)
            ).add(new JsonObject()
                .put("uid", "_:sw1")
                .put("starring", new JsonObject().put("uid", "_:han"))
            ).add(new JsonObject()
                .put("uid", "_:sw4")
                .put("starring", new JsonObject().put("uid", "_:luke"))
            ).add(new JsonObject()
                    .put("uid", "_:sw4")
                    .put("starring", new JsonObject().put("uid", "_:leia"))
            ).add(new JsonObject()
                    .put("uid", "_:sw4")
                    .put("starring", new JsonObject().put("uid", "_:han"))
            ).add(new JsonObject()
                    .put("uid", "_:sw1")
                    .put("starring", new JsonObject().put("uid", "_:jarjar"))
            );


    @Before
    public void setup(TestContext ctx) {
        final Async async = ctx.async();
        vertx = Vertx.vertx();
        client = new DGraphClient(vertx);
        client.alter(DROP_ALL, res -> {
            if (res.failed()) {
                ctx.fail(res.cause());
                return;
            }
            client.alter(CREATE_SCHEMA, createSchemaRes -> {
                if (createSchemaRes.failed()) {
                    ctx.fail(createSchemaRes.cause());
                    return;
                }
                final DgraphProto.Mutation insertData = DgraphProto.Mutation
                        .newBuilder()
                        .setCommitNow(true)
                        .setSetJson(str(SW_DATA))
                        .build();
                //async.complete();
                client.mutate(insertData, failOrComplete(ctx, async));
            });
        });
    }

    @After
    public void tearDown(TestContext ctx) {
        client.alter(DROP_ALL, failOrComplete(ctx));
    }

    @Test
    public void simpleQuery(TestContext context) {
        final Async async = context.async();
        final String queryName = "old_movies";
        final DgraphProto.Request newStarWars = DgraphProto.Request
                .newBuilder()
                .setQuery("{ " + queryName + "(func:allofterms(name, \"Star Wars\")) @filter(ge(release_date, \"1980\")) {\n" +
                        "    name\n" +
                        "    release_date\n" +
                        "    revenue\n" +
                        "    running_time\n" +
                        "    starring {" +
                        "     name\n" +
                        "    }\n" +
                        "  }" +
                        "}")
                .build();
        client.query(newStarWars, queryRes -> {
            if (queryRes.failed()) {
                context.fail(queryRes.cause());
                return;
            }
            final DgraphProto.Response resp = queryRes.result();
            final JsonObject json = new JsonObject(resp.getJson().toStringUtf8());
            final JsonArray queryResults = json.getJsonArray(queryName);
            context.assertNotNull(queryResults);
            context.assertEquals(1, queryResults.size());
            final JsonObject badMovie = queryResults.getJsonObject(0);
            context.assertNotNull(badMovie);
            final String releaseISO = badMovie.getString("release_date");
            final Calendar releaseDate = DatatypeConverter.parseDateTime(releaseISO);
            final int year = releaseDate.get(Calendar.YEAR);
            context.assertTrue(year >= 1980);
            async.complete();
        });
    }

    @Test
    public void cascadeQuery(TestContext context) {
        final Async async = context.async();
        final String queryName = "with_jarjar";
        final DgraphProto.Request newStarWars = DgraphProto.Request
                .newBuilder()
                .setQuery("{ " + queryName + "(func:allofterms(name, \"Star Wars\")) @cascade {\n" +
                        "    name\n" +
                        "    release_date\n" +
                        "    revenue\n" +
                        "    running_time\n" +
                        "    starring @filter(eq(name, \"Jarjar Binks\")) {\n" +
                        "     name\n" +
                        "    }\n" +
                        "  }" +
                        "}")
                .build();
        client.query(newStarWars, queryRes -> {
            if (queryRes.failed()) {
                context.fail(queryRes.cause());
                return;
            }
            final DgraphProto.Response resp = queryRes.result();
            final JsonObject json = new JsonObject(resp.getJson().toStringUtf8());
            final JsonArray queryResults = json.getJsonArray(queryName);
            context.assertNotNull(queryResults);
            context.assertEquals(1, queryResults.size());
            final JsonObject badMovie = queryResults.getJsonObject(0);
            context.assertNotNull(badMovie);
            final JsonArray badMovieCharacters = badMovie.getJsonArray("starring");
            context.assertNotNull(badMovieCharacters);
            context.assertEquals(1, badMovieCharacters.size());
            final JsonObject jarjar = badMovieCharacters.getJsonObject(0);
            context.assertNotNull(jarjar);
            context.assertEquals("Jarjar Binks", jarjar.getString("name"));
            async.complete();
        });
    }


    private static <T> Handler<AsyncResult<T>> failOrComplete(TestContext ctx) {
        return failOrComplete(ctx, ctx.async());
    }

    private static <T> Handler<AsyncResult<T>> failOrComplete(TestContext ctx, Async async) {
        return res -> {
            if (res.failed()) ctx.fail(res.cause());
            else async.complete();
        };
    }


    private static ByteString str(JsonArray json) {
        return ByteString.copyFromUtf8(json.encode());
    }

    private static ByteString str(JsonObject json) {
        return ByteString.copyFromUtf8(json.encode());
    }

    private static JsonObject character(String name) {
        return new JsonObject()
                .put("uid", "_:" + name.split(" ")[0].toLowerCase())
                .put("name", name);
    }
}
