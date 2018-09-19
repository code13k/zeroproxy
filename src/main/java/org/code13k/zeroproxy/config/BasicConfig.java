package org.code13k.zeroproxy.config;

import com.google.common.io.Resources;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public abstract class BasicConfig {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(BasicConfig.class);

    // Const
    public static final String DEFAULT_CONFIG_BASE_PATH = "config/";
    public static final String CONFIG_BASE_PATH = "config/";

    // Data
    private String mConfigAbsolutePath = null;

    /**
     * Initialize
     */
    public void init() throws Exception {
        // Config Absolute File Path
        String userDir = System.getProperty("user.dir");
        mConfigAbsolutePath = userDir + "/" + CONFIG_BASE_PATH + getConfigFilename();

        // Init config
        if (false == initConfig()) {
            String errorMessage = "Failed to initialize " + mConfigAbsolutePath;
            mLogger.error(errorMessage);
            throw new Exception(errorMessage);
        }

        // Load config
        String content = new String(Files.readAllBytes(Paths.get(mConfigAbsolutePath)));
        if(false == loadConfig(content, mConfigAbsolutePath)){
            String errorMessage = "Failed to load config " + mConfigAbsolutePath;
            mLogger.error(errorMessage);
            throw new Exception(errorMessage);
        }

        // End
        logging();
    }

    /**
     * Initialize config file
     */
    private boolean initConfig() {
        // Init Config File
        File file = new File(mConfigAbsolutePath);
        if (file.isFile() == false) {
            mLogger.debug(mConfigAbsolutePath + " is not existed.");
            try {
                FileUtils.forceMkdirParent(file);
                String resourcePath = DEFAULT_CONFIG_BASE_PATH + getDefaultConfigFilename();
                String configContent = Resources.toString(Resources.getResource(resourcePath), Charset.defaultCharset());
                BufferedWriter out = new BufferedWriter(new FileWriter(mConfigAbsolutePath));
                out.write(configContent);
                out.close();
            } catch (Exception e) {
                mLogger.error("Failed to initialize " + mConfigAbsolutePath, e);
                return false;
            }
        }

        // End
        return true;
    }

    /**
     * Get configuration file name
     */
    protected abstract String getConfigFilename();

    /**
     * Get default configuration file name in resource folder
     */
    protected abstract String getDefaultConfigFilename();

    /**
     * Load configuration file
     */
    protected abstract boolean loadConfig(final String content, final String filePath);

    /**
     * Print log
     */
    protected abstract void logging();
}
