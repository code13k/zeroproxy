package org.code13k.zeroproxy.model.config.proxy;

import org.code13k.zeroproxy.model.BasicModel;

import java.util.ArrayList;

public class ProxyHttpInfo extends BasicModel {
    private String location;
    private int connectTimeout;
    private int idleTimeout;
    private ArrayList<String> targets;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    public ArrayList<String> getTargets() {
        return targets;
    }

    public void setTargets(ArrayList<String> targets) {
        this.targets = targets;
    }
}
