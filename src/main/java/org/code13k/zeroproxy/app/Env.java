package org.code13k.zeroproxy.app;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Env {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(Env.class);

    // Data
    private String mHostname = "";
    private String mIP = "";
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

        // Version
        try {
            mVersionString = parseApplicationVersion();
        } catch (Exception e) {
            mLogger.error("Failed to get version", e);
        }

        // Jar File Name
        mJarFilename = parseJarFilename();

        // End
        logging();
    }

    /**
     * Logging
     */
    public void logging() {
        Map<String, Object> values = values();
        mLogger.info("------------------------------------------------------------------------");
        mLogger.info("Application Environments");
        mLogger.info("------------------------------------------------------------------------");
        values.forEach((k, v) -> mLogger.info(k + " = " + v));
        mLogger.info("------------------------------------------------------------------------");
    }

    /**
     * Get all values
     */
    public Map<String, Object> values() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("hostname", getHostname());
        result.put("ip", getIP());
        result.put("cpuProcessorCount", getProcessorCount());
        result.put("applicationVersion", getVersionString());
        result.put("jarFile", getJarFilename());
        result.put("javaVersion", getJavaVersion());
        result.put("javaVendor", getJavaVendor());
        result.put("osVersion", getOsVersion());
        result.put("osName", getOsName());

        return result;
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
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * String of app version
     */
    public String getVersionString() {
        return mVersionString;
    }

    /**
     * Filename of Jar
     */
    public String getJarFilename() {
        return mJarFilename;
    }

    /**
     * Get java version
     */
    public String getJavaVersion() {
        return System.getProperty("java.version");
    }

    /**
     * Get java vendor
     */
    public String getJavaVendor() {
        return System.getProperty("java.vendor");
    }

    /**
     * Get OS name
     */
    public String getOsName() {
        return System.getProperty("os.name");
    }

    /**
     * Get OS version
     */
    public String getOsVersion() {
        return System.getProperty("os.version");
    }

    /**
     * Get app version from manifest info
     */
    private String parseApplicationVersion() {
        Enumeration resourceEnum;
        try {
            resourceEnum = Thread.currentThread().getContextClassLoader().getResources(JarFile.MANIFEST_NAME);
            while (resourceEnum.hasMoreElements()) {
                try {
                    URL url = (URL) resourceEnum.nextElement();
                    InputStream is = url.openStream();
                    if (is != null) {
                        Manifest manifest = new Manifest(is);
                        Attributes attr = manifest.getMainAttributes();
                        String version = attr.getValue("Implementation-Version");
                        if (version != null) {
                            return version;
                        }
                    }
                } catch (Exception e) {
                    // Nothing
                }
            }
        } catch (IOException e1) {
            // Nothing
        }
        return null;
    }

    /**
     * Get jar file name from system property
     */
    private String parseJarFilename() {
        String javaClassPath = System.getProperty("java.class.path");
        String jarFilename = "";
        if (StringUtils.isBlank(javaClassPath) == false) {
            String[] temp = StringUtils.split(javaClassPath, "/");
            if (temp.length > 0) {
                jarFilename = temp[temp.length - 1];
            }
        }
        return jarFilename;
    }
}