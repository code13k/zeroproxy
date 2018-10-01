package org.code13k.zeroproxy.service.api.controller;

import org.code13k.zeroproxy.app.Env;
import org.code13k.zeroproxy.app.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

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
