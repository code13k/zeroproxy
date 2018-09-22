package org.code13k.zeroproxy.model.config.app;

import org.code13k.zeroproxy.model.BasicModel;

public class PortInfo extends BasicModel {
    private int proxyHttp;
    private int proxyWs;
    private int apiHttp;

    public int getProxyHttp() {
        return proxyHttp;
    }

    public void setProxyHttp(int proxyHttp) {
        this.proxyHttp = proxyHttp;
    }

    public int getProxyWs() {
        return proxyWs;
    }

    public void setProxyWs(int proxyWs) {
        this.proxyWs = proxyWs;
    }

    public int getApiHttp() {
        return apiHttp;
    }

    public void setApiHttp(int apiHttp) {
        this.apiHttp = apiHttp;
    }
}
