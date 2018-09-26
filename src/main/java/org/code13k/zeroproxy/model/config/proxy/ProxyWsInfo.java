package org.code13k.zeroproxy.model.config.proxy;

import org.code13k.zeroproxy.model.BasicModel;

import java.util.ArrayList;

public class ProxyWsInfo extends BasicModel {
    private String location;
    private ArrayList<String> targets;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ArrayList<String> getTargets() {
        return targets;
    }

    public void setTargets(ArrayList<String> targets) {
        this.targets = targets;
    }
}
