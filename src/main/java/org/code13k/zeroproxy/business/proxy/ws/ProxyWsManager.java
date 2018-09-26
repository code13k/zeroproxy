package org.code13k.zeroproxy.business.proxy.ws;

import io.vertx.core.MultiMap;
import org.code13k.zeroproxy.config.ProxyWsConfig;
import org.code13k.zeroproxy.model.config.proxy.ProxyWsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


public class ProxyWsManager {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(ProxyWsManager.class);

    // Data
    private ProxyWsClient mClient = null;
    private long mSentTextCount = 0;
    private long mConnectedCount = 0;

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final ProxyWsManager INSTANCE = new ProxyWsManager();
    }

    public static ProxyWsManager getInstance() {
        return ProxyWsManager.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private ProxyWsManager() {
        mLogger.trace("ProxyWsManager()");
    }

    /**
     * Initialize
     */
    synchronized public void init() {
        if (mClient == null) {
            mClient = new ProxyWsClient();
        } else {
            mLogger.debug("Duplicated initializing");
        }
    }

    /**
     * Connect
     */
    public String connect(int channelIndex, String originPath, MultiMap originHeaders) {
        ArrayList<String> uriList = new ArrayList<>();
        ProxyWsInfo proxyWsInfo = ProxyWsConfig.getInstance().getChannel(channelIndex);
        proxyWsInfo.getTargets().forEach(target -> {
            String uri = target + "/" + originPath;
            uriList.add(uri);
        });
        String connectionId = mClient.connect(uriList, originHeaders);
        mLogger.debug("connectionId = " + connectionId);
        mConnectedCount++;
        return connectionId;
    }

    /**
     * Disconnect
     */
    public void disconnect(String connectionId){
        mClient.disconnect(connectionId);
        mConnectedCount--;
    }

    /**
     * Send text
     */
    public void sendText(String connectionId, String message) {
        mClient.sendText(connectionId, message);
        mSentTextCount++;
    }

    /**
     * Get sent text count
     */
    public long getSentTextCount() {
        return mSentTextCount;
    }

    /**
     * Get connected count
     */
    public long getConnectedCount(){
        return mConnectedCount;
    }
}
