package org.code13k.zeroproxy.app;

import org.apache.commons.lang3.StringUtils;
import org.code13k.zeroproxy.lib.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;

public class Env {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(Env.class);

    // Data
    private String mHostname = "";
    private String mIP = "";
    private int mProcessorCount = 0;
    private String mVersionString = "";
    private String mJarFilename = "";


    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final Env INSTANCE = new Env();
    }

    public static Env getInstance() {
        return Env.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private Env() {
        mLogger.trace("Env()");
    }

    /**
     * Initialize
     */
    public void init() {
        // Hostname
        try {
            mHostname = InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            mLogger.error("Failed to get hostname", e);
        }

        // IP
        try {
            InetAddress ip = InetAddress.getLocalHost();
            mIP = ip.getHostAddress();
        } catch (Exception e) {
            mLogger.error("Failed to get IP", e);
        }

        // Processor Count
        try {
            mProcessorCount = Runtime.getRuntime().availableProcessors();
        } catch (Exception e) {
            mLogger.error("Filed to get processor count", e);
        }

        // Version
        try {
            mVersionString = Util.getApplicationVersion();
        } catch (Exception e) {
            mLogger.error("Failed to get version", e);
        }

        // Jar File Name
        String javaClassPath = System.getProperty("java.class.path");
        String jarFilename = "";
        if (StringUtils.isBlank(javaClassPath) == false) {
            String[] temp = StringUtils.split(javaClassPath, "/");
            if (temp.length > 0) {
                jarFilename = temp[temp.length - 1];
            }
        }
        mJarFilename = jarFilename;

        // End
        logging();
    }

    /**
     * Logging
     */
    public void logging() {
        // Begin
        mLogger.info("------------------------------------------------------------------------");
        mLogger.info("Application Environments");
        mLogger.info("------------------------------------------------------------------------");

        // Hostname
        mLogger.info("Hostname = " + mHostname);

        // IP
        mLogger.info("IP = " + mIP);

        // Processor Count
        mLogger.info("Processor count = " + mProcessorCount);

        // Version
        mLogger.info("Version = " + mVersionString);

        // Jar File Name
        mLogger.info("Jar filename = " + mJarFilename);

        // End
        mLogger.info("------------------------------------------------------------------------");
    }

    /**
     * String of app version
     */
    public String getVersionString() {
        return mVersionString;
    }

    /**
     * Hostname of server
     */
    public String getHostname() {
        return mHostname;
    }

    /**
     * IP of server
     */
    public String getIP() {
        return mIP;
    }

    /**
     * Processor count of server
     */
    public int getProcessorCount() {
        return mProcessorCount;
    }

    /**
     * Filename of Jar
     */
    public String getJarFilename(){
        return mJarFilename;
    }
}