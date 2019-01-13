package io.vertx.dgraph;

public class DGraphClientOptions {

    private String host;
    private int port;
    private boolean usePlainText;

    public DGraphClientOptions(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public DGraphClientOptions setHost(String host) {
        this.host = host;
        return this;
    }

    public int getPort() {
        return port;
    }

    public DGraphClientOptions setPort(int port) {
        this.port = port;
        return this;
    }

    public boolean isUsePlainText() {
        return usePlainText;
    }

    public DGraphClientOptions setUsePlainText(boolean usePlainText) {
        this.usePlainText = usePlainText;
        return this;
    }
}
