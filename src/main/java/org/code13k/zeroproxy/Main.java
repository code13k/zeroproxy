package org.code13k.zeroproxy;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.code13k.zeroproxy.business.proxy.http.ProxyHttpManager;
import org.code13k.zeroproxy.business.proxy.ws.ProxyWsManager;
import org.code13k.zeroproxy.config.AppConfig;
import org.code13k.zeroproxy.config.LogConfig;
import org.code13k.zeroproxy.config.ProxyHttpConfig;
import org.code13k.zeroproxy.app.Env;
import org.code13k.zeroproxy.app.Status;
import org.code13k.zeroproxy.config.ProxyWsConfig;
import org.code13k.zeroproxy.service.api.ApiHttpServer;
import org.code13k.zeroproxy.service.proxy.ProxyHttpServer;
import org.code13k.zeroproxy.service.proxy.ProxyWsServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    /**
     * This is a exceptional code for logging.
     * It depends on LogConfig class.
     * If you modified it, you must modify LogConfig class.
     *
     * @see org.code13k.zeroproxy.config.LogConfig
     */
    static {
        System.setProperty("logback.configurationFile", "config/logback.xml");
    }

    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(Main.class);

    /**
     * Main
     *
     * @param args arguments
     */
    public static void main(String[] args) {
        // Logs
        mLogger.trace("This is TRACE Log!");
        mLogger.debug("This is DEBUG Log!");
        mLogger.info("This is INFO Log!");
        mLogger.warn("This is WARN Log!");
        mLogger.error("This is ERROR Log!");

        // Arguments
        if (args != null) {
            int argsLength = args.length;
            if (argsLength > 0) {
                mLogger.info("------------------------------------------------------------------------");
                mLogger.info("Arguments");
                mLogger.info("------------------------------------------------------------------------");
                for (int i = 0; i < argsLength; i++) {
                    mLogger.info("Args " + i + " = " + args[i]);
                }
                mLogger.info("------------------------------------------------------------------------");

            }
        }

        // System Properties
        mLogger.debug("------------------------------------------------------------------------");
        mLogger.debug("System Property");
        mLogger.debug("------------------------------------------------------------------------");
        System.getProperties().forEach((key, value) -> {
            mLogger.debug(key + " = " + value);
        });
        mLogger.debug("------------------------------------------------------------------------");

        // Initialize
        try {
            LogConfig.getInstance().init();
            AppConfig.getInstance().init();
            ProxyHttpConfig.getInstance().init();
            ProxyWsConfig.getInstance().init();
            Env.getInstance().init();
            Status.getInstance().init();
            ProxyHttpManager.getInstance().init();
            ProxyWsManager.getInstance().init();
        } catch (Exception e) {
            mLogger.error("Failed to initialize", e);
            System.exit(1);
        }

        // Deploy ProxyHttpServer
        try {
            DeploymentOptions options = new DeploymentOptions();
            options.setInstances(Math.max(1, Env.getInstance().getProcessorCount() / 2));
            Vertx.vertx().deployVerticle(ProxyHttpServer.class.getName(), options);
            Thread.sleep(1000);
        } catch (Exception e) {
            mLogger.error("Failed to deploy ProxyHttpServer", e);
            System.exit(2);
        }

        // Deploy ProxyWsServer
        try {
            DeploymentOptions options = new DeploymentOptions();
            options.setInstances(Math.max(1, Env.getInstance().getProcessorCount() / 2));
            Vertx.vertx().deployVerticle(ProxyWsServer.class.getName(), options);
            Thread.sleep(1000);
        } catch (Exception e) {
            mLogger.error("Failed to deploy ProxyWsServer", e);
            System.exit(3);
        }

        // Deploy APIHttpServer
        try {
            DeploymentOptions options = new DeploymentOptions();
            options.setInstances(Math.max(1, Env.getInstance().getProcessorCount() / 2));
            Vertx.vertx().deployVerticle(ApiHttpServer.class.getName(), options);
            Thread.sleep(1000);
        } catch (Exception e) {
            mLogger.error("Failed to deploy ApiHttpServer", e);
            System.exit(4);
        }

        // End
        mLogger.info("Running application is successful.");
    }
}
