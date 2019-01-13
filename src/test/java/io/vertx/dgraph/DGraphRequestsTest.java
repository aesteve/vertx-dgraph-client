package io.vertx.dgraph;

import io.dgraph.DgraphProto;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.junit5.VertxExtension;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.xml.bind.DatatypeConverter;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(VertxExtension.class)
class DGraphRequestsTest extends TestBase {

    @Test
    void simpleQuery(VertxTestContext ctx) {
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
                ctx.failNow(queryRes.cause());
                return;
            }
            final DgraphProto.Response resp = queryRes.result();
            final JsonObject json = new JsonObject(resp.getJson().toStringUtf8());
            final JsonArray queryResults = json.getJsonArray(queryName);
            ctx.verify(() -> {
                assertNotNull(queryResults);
                assertEquals(1, queryResults.size());
                final JsonObject badMovie = queryResults.getJsonObject(0);
                assertNotNull(badMovie);
                final String releaseISO = badMovie.getString("release_date");
                final Calendar releaseDate = DatatypeConverter.parseDateTime(releaseISO);
                final int year = releaseDate.get(Calendar.YEAR);
                assertTrue(year >= 1980);
                ctx.completeNow();
            });
        });
    }

    @Test
    void cascadeQuery(VertxTestContext ctx) {
        final String queryName = "with_jarjar";
        final DgraphProto.Request jarjarMovies = DgraphProto.Request
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
        client.query(jarjarMovies, queryRes -> {
            if (queryRes.failed()) {
                ctx.failNow(queryRes.cause());
                return;
            }
            final DgraphProto.Response resp = queryRes.result();
            final JsonObject json = new JsonObject(resp.getJson().toStringUtf8());
            final JsonArray queryResults = json.getJsonArray(queryName);
            ctx.verify(() -> {
                assertNotNull(queryResults);
                assertEquals(1, queryResults.size());
                final JsonObject badMovie = queryResults.getJsonObject(0);
                assertNotNull(badMovie);
                final JsonArray badMovieCharacters = badMovie.getJsonArray("starring");
                assertNotNull(badMovieCharacters);
                assertEquals(1, badMovieCharacters.size());
                final JsonObject jarjar = badMovieCharacters.getJsonObject(0);
                assertNotNull(jarjar);
                assertEquals("Jarjar Binks", jarjar.getString("name"));
                ctx.completeNow();
            });
        });
    }


}
