package org.code13k.zeroproxy.model;

import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;

public class ProxyResponse extends BasicModel {
    private int statusCode;
    private String statusMessage;
    private MultiMap headers;
    private Buffer body;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public MultiMap getHeaders() {
        return headers;
    }

    public void setHeaders(MultiMap headers) {
        this.headers = headers;
    }

    public Buffer getBody() {
        return body;
    }

    public void setBody(Buffer body) {
        this.body = body;
    }
}
