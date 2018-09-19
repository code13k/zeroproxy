package org.code13k.zeroproxy.model.config.app;

import org.code13k.zeroproxy.model.BasicModel;

public class PortInfo extends BasicModel {
    private int mainHttp;
    private int mainWs;
    private int apiHttp;

    public int getMainHttp() {
        return mainHttp;
    }

    public void setMainHttp(int mainHttp) {
        this.mainHttp = mainHttp;
    }

    public int getMainWs() {
        return mainWs;
    }

    public void setMainWs(int mainWs) {
        this.mainWs = mainWs;
    }

    public int getApiHttp() {
        return apiHttp;
    }

    public void setApiHttp(int apiHttp) {
        this.apiHttp = apiHttp;
    }
}
