package org.code13k.zeroproxy.service.api.controller;

import org.code13k.zeroproxy.app.Env;
import org.code13k.zeroproxy.app.Status;
import org.code13k.zeroproxy.config.AppConfig;
import org.code13k.zeroproxy.model.config.app.PortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AppAPI extends BasicAPI {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(AppAPI.class);

    /**
     * Environment
     */
    public String env() {
        return toResultJsonString(Env.getInstance().values());
    }

    /**
     * status
     */
    public String status() {
        return toResultJsonString(Status.getInstance().values());
    }

    /**
     * config
     */
    public String config(){
        PortInfo portInfo = AppConfig.getInstance().getPort();
        Map<String, Object> result = new HashMap<>();
        result.put("port", portInfo.toMap());
        return toResultJsonString(result);
    }

    /**
     * hello, world
     */
    public String hello() {
        return toResultJsonString("world");
    }

    /**
     * ping-pong
     */
    public String ping() {
        return toResultJsonString("pong");
    }

}
