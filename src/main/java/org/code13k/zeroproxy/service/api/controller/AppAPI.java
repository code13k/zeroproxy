package org.code13k.zeroproxy.service.api.controller;

import org.code13k.zeroproxy.app.Env;
import org.code13k.zeroproxy.app.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;

public class AppAPI extends BasicAPI {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(AppAPI.class);

    /**
     * info
     */
    public String info() {
        HashMap<String, Object> result = new HashMap<>();

        // Hostname
        String hostname = Env.getInstance().getHostname();
        result.put("hostname", hostname);

        // Application Version
        String applicationVersion = Env.getInstance().getVersionString();
        result.put("applicationVersion", applicationVersion);

        // Java Version
        String javaVersion = System.getProperty("java.version");
        result.put("javaVersion", javaVersion);

        // Java Vendor
        String javaVendor = System.getProperty("java.vendor");
        result.put("javaVendor", javaVendor);

        // OS Name
        String osName = System.getProperty("os.name");
        result.put("osName", osName);

        // OS Version
        String osVersion = System.getProperty("os.version");
        result.put("osVersion", osVersion);

        // Jar File Name
        result.put("jarFile", Env.getInstance().getJarFilename());

        // Current Date
        result.put("currentDate", Status.getInstance().getCurrentDateString());

        // Started Date
        result.put("startedDate", Status.getInstance().getAppStartedDateString());

        // CPU Processor Count
        int cpuProcessorCount = Env.getInstance().getProcessorCount();
        result.put("cpuProcessorCount", cpuProcessorCount);

        // CPU Usage
        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        double cpuUsage = operatingSystemMXBean.getSystemLoadAverage();
        result.put("cpuUsage", new Double(new DecimalFormat("#.##").format(cpuUsage)));

        // VM Memory Usage
        Runtime runtime = Runtime.getRuntime();
        NumberFormat format = NumberFormat.getInstance();
        long vmMemoryMax = runtime.maxMemory();
        long vmMemoryAllocated = runtime.totalMemory();
        long vmMemoryFree = runtime.freeMemory();
        String vmMemoryMaxString = format.format(vmMemoryMax / 1024 / 1024) + "M";
        String vmMemoryAllocatedString = format.format(vmMemoryAllocated / 1024 / 1024) + "M";
        String vmMemoryFreeString = format.format(vmMemoryFree / 1024 / 1024) + "M";
        String vmMemoryTotalFreeString = format.format((vmMemoryFree + (vmMemoryMax - vmMemoryAllocated)) / 1024 / 1024) + "M";
        result.put("vmMemoryMax", vmMemoryMaxString);
        result.put("vmMemoryAllocated", vmMemoryAllocatedString);
        result.put("vmMemoryFree", vmMemoryFreeString);
        result.put("vmMemoryTotalFree", vmMemoryTotalFreeString);

        // Thread Info
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] threadIds = threadMXBean.getAllThreadIds();
        HashMap<Long, String> threadResult = new HashMap<>();
        for (long threadId : threadIds) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadId);
            String threadInfoValue = threadInfo.getThreadName() + ", " + format.format(threadMXBean.getThreadCpuTime(threadId) / 1000000000) + "sec";
            threadResult.put(threadId, threadInfoValue);
        }
        int threadCount = threadMXBean.getThreadCount();
        result.put("threadInfo", threadResult);
        result.put("threadCount", threadCount);

        // Running Time
        result.put("runningTimeHour", Status.getInstance().getAppRunningTimeHour());

        // End
        return toResultJsonString(result);
    }

    /**
     * hello, world
     */
    public String hello() {
        return toResultJsonString("world");
    }

    /**
     * ping-pong
     */
    public String ping() {
        return toResultJsonString("pong");
    }

}
