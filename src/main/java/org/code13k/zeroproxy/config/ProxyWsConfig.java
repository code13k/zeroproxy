package org.code13k.zeroproxy.config;

import org.apache.commons.lang3.StringUtils;
import org.code13k.zeroproxy.model.config.proxy.ProxyHttpInfo;
import org.code13k.zeroproxy.model.config.proxy.ProxyWsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ProxyWsConfig extends BasicConfig {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(ProxyWsConfig.class);

    // Data
    private ArrayList<ProxyWsInfo> mChannelList = new ArrayList<>();

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final ProxyWsConfig INSTANCE = new ProxyWsConfig();
    }

    public static ProxyWsConfig getInstance() {
        return ProxyWsConfig.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private ProxyWsConfig() {
        mLogger.trace("ProxyWsConfig()");
    }

    /**
     * Get channel
     */
    public ProxyWsInfo getChannel(int index) {
        if (index < mChannelList.size()) {
            ProxyWsInfo proxyWsInfo = mChannelList.get(index);
            mLogger.trace("proxyWsInfo = " + proxyWsInfo);
            return proxyWsInfo;
        }
        return null;
    }

    /**
     * Get all channel data
     */
    public ArrayList<ProxyWsInfo> getChannelList() {
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
        return "default_proxy_ws_config.yml";
    }

    @Override
    protected String getConfigFilename() {
        return "proxy_ws_config.yml";
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
                ProxyWsInfo proxyWsInfo = new ProxyWsInfo();

                // Location
                String location = (String) itemObject.get("location");

                // Targets
                ArrayList<String> targets = (ArrayList<String>) itemObject.get("targets");

                // Set Channel
                proxyWsInfo.setLocation(location);
                proxyWsInfo.setTargets(targets);

                // Check validation
                if (StringUtils.isEmpty(proxyWsInfo.getLocation()) == true) {
                    mLogger.error("Invalid channel (location is invalid)");
                } else if (proxyWsInfo.getTargets() == null || proxyWsInfo.getTargets().size() == 0) {
                    mLogger.error("Invalid channel (targets is invalid)");
                } else {
                    mChannelList.add(proxyWsInfo);
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
        mLogger.info("Proxy WS Configuration");
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
