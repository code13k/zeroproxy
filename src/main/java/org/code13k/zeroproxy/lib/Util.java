package org.code13k.zeroproxy.lib;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class Util {

    /**
     * Check if the given number is valid port number.
     */
    public static boolean isValidPortNumber(Integer portNumber) {
        if (portNumber == null || portNumber < 1 || portNumber > 65535) {
            return false;
        }
        return true;
    }
}
