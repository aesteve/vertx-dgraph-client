package io.vertx.dgraph.data;

import com.google.protobuf.ByteString;
import io.dgraph.DgraphProto;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

public class TestSet {

    public static final DgraphProto.Operation DROP_ALL = DgraphProto.Operation
            .newBuilder()
            .setDropAll(true)
            .build();

    public static final DgraphProto.Operation CREATE_SCHEMA = DgraphProto.Operation
            .newBuilder()
            .setSchema(
                    "name: string @index(term) @upsert .\n" +
                            "release_date: datetime @index(year) .\n" +
                            "revenue: float .\n" +
                            "running_time: int ."
            )
            .build();

    public static final JsonArray SW_DATA = new JsonArray()
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
                    .put("uid", "_:sw1")
                    .put("starring", new JsonObject().put("uid", "_:jarjar"))
            ).add(new JsonObject()
                    .put("uid", "_:sw4")
                    .put("starring", new JsonObject().put("uid", "_:luke"))
            ).add(new JsonObject()
                    .put("uid", "_:sw4")
                    .put("starring", new JsonObject().put("uid", "_:leia"))
            ).add(new JsonObject()
                    .put("uid", "_:sw4")
                    .put("starring", new JsonObject().put("uid", "_:han"))
            );

    public static ByteString str(JsonArray json) {
        return ByteString.copyFromUtf8(json.encode());
    }

    public static ByteString str(JsonObject json) {
        return ByteString.copyFromUtf8(json.encode());
    }

    private static JsonObject character(String name) {
        return new JsonObject()
                .put("uid", "_:" + name.split(" ")[0].toLowerCase())
                .put("name", name);
    }

}
