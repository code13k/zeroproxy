package org.code13k.zeroproxy.config;

import org.apache.commons.lang3.StringUtils;
import org.code13k.zeroproxy.model.config.proxy.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ProxyConfig extends BasicConfig {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(ProxyConfig.class);

    // Data
    private ArrayList<ProxyInfo> mChannelList = new ArrayList<>();

    /**
     * Channel Type
     */
    public class ChannelType {
        public static final String ROUND_ROBIN = "round-robin";
        public static final String RANDOM = "random";
        public static final String ALL = "all";
    }

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final ProxyConfig INSTANCE = new ProxyConfig();
    }

    public static ProxyConfig getInstance() {
        return ProxyConfig.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private ProxyConfig() {
        mLogger.trace("ProxyConfig()");
    }

    /**
     * Get channel
     */
    public ProxyInfo getChannel(int index) {
        if (index < mChannelList.size()) {
            ProxyInfo proxyInfo = mChannelList.get(index);
            mLogger.trace("proxyInfo = " + proxyInfo);
            return proxyInfo;
        }
        return null;
    }

    /**
     * Get all channel data
     */
    public ArrayList<ProxyInfo> getChannelList() {
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
        return "default_proxy_config.yml";
    }

    @Override
    protected String getConfigFilename() {
        return "proxy_config.yml";
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
                ProxyInfo proxyInfo = new ProxyInfo();

                // Location
                String location = (String) itemObject.get("location");

                // Type
                String type = (String) itemObject.get("type");

                // Timeout
                int connectTimeout = (Integer) itemObject.getOrDefault("connect_timeout", 0);
                int idleTimeout = (Integer) itemObject.getOrDefault("idle_timeout", 0);

                // Targets
                ArrayList<String> targets = (ArrayList<String>) itemObject.get("targets");

                // Set Channel
                proxyInfo.setLocation(location);
                proxyInfo.setType(type);
                proxyInfo.setConnectTimeout(connectTimeout);
                proxyInfo.setIdleTimeout(idleTimeout);
                proxyInfo.setTargets(targets);

                // Check validation
                if(StringUtils.isEmpty(proxyInfo.getLocation())==true){
                    mLogger.error("Invalid channel (location is invalid)");
                } else if (type.equalsIgnoreCase(ChannelType.ROUND_ROBIN) == false
                        && type.equalsIgnoreCase(ChannelType.RANDOM) == false
                        && type.equalsIgnoreCase(ChannelType.ALL) == false) {
                    mLogger.error("Invalid channel (type is invalid)");
                } else if (proxyInfo.getConnectTimeout() <= 0) {
                    mLogger.error("Invalid channel (connect_timeout is invalid)");
                } else if (proxyInfo.getIdleTimeout() <= 0) {
                    mLogger.error("Invalid channel (idle_timeout is invalid)");
                } else if (proxyInfo.getTargets() == null || proxyInfo.getTargets().size() == 0) {
                    mLogger.error("Invalid channel (targets is invalid)");
                } else {
                    mChannelList.add(proxyInfo);
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
        mLogger.info("Proxy Configuration");
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
