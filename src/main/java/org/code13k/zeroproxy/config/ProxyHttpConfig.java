package org.code13k.zeroproxy.config;

import org.apache.commons.lang3.StringUtils;
import org.code13k.zeroproxy.model.config.proxy.ProxyHttpInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ProxyHttpConfig extends BasicConfig {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(ProxyHttpConfig.class);

    // Data
    private ArrayList<ProxyHttpInfo> mChannelList = new ArrayList<>();

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final ProxyHttpConfig INSTANCE = new ProxyHttpConfig();
    }

    public static ProxyHttpConfig getInstance() {
        return ProxyHttpConfig.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private ProxyHttpConfig() {
        mLogger.trace("ProxyHttpConfig()");
    }

    /**
     * Get channel
     */
    public ProxyHttpInfo getChannel(int index) {
        if (index < mChannelList.size()) {
            ProxyHttpInfo proxyHttpInfo = mChannelList.get(index);
            mLogger.trace("proxyHttpInfo = " + proxyHttpInfo);
            return proxyHttpInfo;
        }
        return null;
    }

    /**
     * Get all channel data
     */
    public ArrayList<ProxyHttpInfo> getChannelList() {
        return new ArrayList<>(mChannelList);
    }

    /**
     * Get registered channel size
     */
    public int size() {
        return mChannelList.size();
    }

    @Override
    protected String getDefaultConfigFilename() {
        return "default_proxy_http_config.yml";
    }

    @Override
    protected String getConfigFilename() {
        return "proxy_http_config.yml";
    }

    @Override
    protected boolean loadConfig(final String content, final String filePath) {
        try {
            Yaml yaml = new Yaml();
            ArrayList<Object> yamlObject = yaml.load(content);
            mLogger.trace("yamlObject class name = " + yamlObject.getClass().getName());
            mLogger.trace("yamlObject = " + yamlObject);

            // Get data
            yamlObject.forEach(item -> {
                mLogger.trace("key=" + item);
                LinkedHashMap itemObject = (LinkedHashMap) item;
                ProxyHttpInfo proxyHttpInfo = new ProxyHttpInfo();

                // Location
                String location = (String) itemObject.get("location");

                // Timeout
                int connectTimeout = (Integer) itemObject.getOrDefault("connect_timeout", 0);
                int idleTimeout = (Integer) itemObject.getOrDefault("idle_timeout", 0);

                // Targets
                ArrayList<String> targets = (ArrayList<String>) itemObject.get("targets");

                // Set Channel
                proxyHttpInfo.setLocation(location);
                proxyHttpInfo.setConnectTimeout(connectTimeout);
                proxyHttpInfo.setIdleTimeout(idleTimeout);
                proxyHttpInfo.setTargets(targets);

                // Check validation
                if (StringUtils.isEmpty(proxyHttpInfo.getLocation()) == true) {
                    mLogger.error("Invalid channel (location is invalid)");
                } else if (proxyHttpInfo.getConnectTimeout() <= 0) {
                    mLogger.error("Invalid channel (connect_timeout is invalid)");
                } else if (proxyHttpInfo.getIdleTimeout() <= 0) {
                    mLogger.error("Invalid channel (idle_timeout is invalid)");
                } else if (proxyHttpInfo.getTargets() == null || proxyHttpInfo.getTargets().size() == 0) {
                    mLogger.error("Invalid channel (targets is invalid)");
                } else {
                    mChannelList.add(proxyHttpInfo);
                }
            });
        } catch (Exception e) {
            mLogger.error("Failed to load config file", e);
            return false;
        }
        return true;
    }

    @Override
    protected void logging() {
        // Begin
        mLogger.info("------------------------------------------------------------------------");
        mLogger.info("Proxy HTTP Configuration");
        mLogger.info("------------------------------------------------------------------------");

        // Config File Path
        mLogger.info("Config file path = " + getConfigFilename());

        // Channel
        mChannelList.forEach(channel -> {
            mLogger.info("- " + channel);
        });

        // End
        mLogger.info("------------------------------------------------------------------------");
    }
}
