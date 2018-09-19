package org.code13k.zeroproxy.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogConfig extends BasicConfig {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(LogConfig.class);

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final LogConfig INSTANCE = new LogConfig();
    }

    public static LogConfig getInstance() {
        return LogConfig.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private LogConfig() {
        mLogger.trace("LogConfig()");
    }

    @Override
    protected String getDefaultConfigFilename() {
        return "default_logback.xml";
    }

    @Override
    protected String getConfigFilename() {
        return "logback.xml";
    }

    @Override
    protected boolean loadConfig(final String content, final String filePath) {
        try {
            // Nothing
        } catch (Exception e) {
            mLogger.error("Failed to load config file", e);
        }
        return true;
    }

    @Override
    public void logging() {
        // Begin
        mLogger.info("------------------------------------------------------------------------");
        mLogger.info("Log Configuration");
        mLogger.info("------------------------------------------------------------------------");

        // Config File Path
        mLogger.info("Config file path = " + getConfigFilename());

        // End
        mLogger.info("------------------------------------------------------------------------");
    }
}
