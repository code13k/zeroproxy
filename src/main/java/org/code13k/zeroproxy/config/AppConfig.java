package org.code13k.zeroproxy.config;

import org.code13k.zeroproxy.lib.Util;
import org.code13k.zeroproxy.model.config.app.PortInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;

public class AppConfig extends BasicConfig {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(AppConfig.class);

    // Data
    private PortInfo mPortInfo = new PortInfo();

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final AppConfig INSTANCE = new AppConfig();
    }

    public static AppConfig getInstance() {
        return AppConfig.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private AppConfig() {
        mLogger.trace("AppConfig()");
    }

    @Override
    protected String getDefaultConfigFilename() {
        return "default_app_config.yml";
    }

    @Override
    protected String getConfigFilename() {
        return "app_config.yaml";
    }

    @Override
    protected boolean loadConfig(final String content, final String filePath) {
        try {
            Yaml yaml = new Yaml();
            LinkedHashMap yamlObject = yaml.load(content);
            mLogger.trace("yamlObject class name = " + yamlObject.getClass().getName());
            mLogger.trace("yamlObject = " + yamlObject);

            // PortInfo
            LinkedHashMap portObject = (LinkedHashMap) yamlObject.get("port");
            mLogger.trace("portObject class name = " + portObject.getClass().getName());
            mLogger.trace("portObject = " + portObject);
            Integer portProxyHttp = (Integer) portObject.get("proxy_http");
            if (Util.isValidPortNumber(portProxyHttp) == false) {
                mLogger.error("Invalid proxy_http port : " + portProxyHttp);
                return false;
            }
            Integer portProxyWs = (Integer) portObject.get("proxy_ws");
            if (Util.isValidPortNumber(portProxyWs) == false) {
                mLogger.error("Invalid proxy_ws port : " + portProxyWs);
                return false;
            }
            Integer portApiHttp = (Integer) portObject.get("api_http");
            if (Util.isValidPortNumber(portApiHttp) == false) {
                mLogger.error("Invalid api_http port : " + portApiHttp);
                return false;
            }
            mPortInfo.setProxyHttp(portProxyHttp);
            mPortInfo.setProxyWs(portProxyWs);
            mPortInfo.setApiHttp(portApiHttp);
        } catch (Exception e) {
            mLogger.error("Failed to load config file", e);
            return false;
        }
        return true;
    }

    @Override
    public void logging() {
        // Begin
        mLogger.info("------------------------------------------------------------------------");
        mLogger.info("Application Configuration");
        mLogger.info("------------------------------------------------------------------------");

        // Config File Path
        mLogger.info("Config file path = " + getConfigFilename());

        // PortInfo
        mLogger.info("proxy_http of PortInfo = " + mPortInfo.getProxyHttp());
        mLogger.info("proxy_ws of PortInfo = " + mPortInfo.getProxyWs());
        mLogger.info("api_http of PortInfo = " + mPortInfo.getApiHttp());

        // End
        mLogger.info("------------------------------------------------------------------------");
    }

    /**
     * Get port
     */
    public PortInfo getPort() {
        return mPortInfo;
    }
}
