package org.code13k.zeroproxy.business.proxy.http;


import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import org.code13k.zeroproxy.app.Env;
import org.code13k.zeroproxy.config.ProxyConfig;
import org.code13k.zeroproxy.model.ProxyResponse;
import org.code13k.zeroproxy.model.config.proxy.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Consumer;

public class ProxyHttpManager {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(ProxyHttpManager.class);

    // Data
    private ArrayList<ProxyHttpClient> mData = null;
    private long mProcessedCount = 0;

    /**
     * Singleton
     */
    private static class SingletonHolder {
        static final ProxyHttpManager INSTANCE = new ProxyHttpManager();
    }

    public static ProxyHttpManager getInstance() {
        return ProxyHttpManager.SingletonHolder.INSTANCE;
    }

    /**
     * Constructor
     */
    private ProxyHttpManager() {
        mLogger.trace("ProxyHttpManager()");
    }

    /**
     * Initialize
     */
    synchronized public void init() {
        if (mData == null) {
            // Event Loop Pool Size
            final int eventLoopPoolSize = Math.max(1, Env.getInstance().getProcessorCount() - 1);

            // Data
            mData = new ArrayList<>();
            ArrayList<ProxyInfo> channelList = ProxyConfig.getInstance().getChannelList();
            channelList.forEach(channel -> {
                ProxyHttpClient client = new ProxyHttpClient(channel, eventLoopPoolSize);
                mData.add(client);
            });
        } else {
            mLogger.debug("Duplicated initializing");
        }
    }

    /**
     * Proxy
     */
    public void proxy(int channelIndex, String originPath, HttpMethod originMethod, MultiMap originHeaders, Buffer originBody, Consumer<ProxyResponse> consumer) {
        ProxyHttpClient client = mData.get(channelIndex);
        client.proxy(originPath, originMethod, originHeaders, originBody, new Consumer<ProxyResponse>() {
            @Override
            public void accept(ProxyResponse proxyResponse) {
                mLogger.trace("Proxy : OK");
                consumer.accept(proxyResponse);
                mProcessedCount++;
            }
        });
    }

    /**
     * Get processed count
     */
    public long getProcessedCount() {
        return mProcessedCount;
    }
}















