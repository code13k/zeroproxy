package org.code13k.zeroproxy.business.proxy.ws;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.http.*;
import io.vertx.core.impl.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProxyWsClient {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(ProxyWsClient.class);

    // Const
    private static final int MAX_CLIENT_COUNT = 1000;

    // Data
    private HttpClient mHttpClient;
    private ConcurrentHashMap<String, ConcurrentHashSet<ConnectionInfo>> mConnectedData;
    private ConcurrentHashMap<String, ConcurrentHashSet<ConnectionInfo>> mConnectingData;
    private Object mConnectionLock;

    /**
     * Connection Info
     */
    private class ConnectionInfo {
        public String connectionId;
        public String uri;
        public MultiMap headers;
        public WebSocket webSocket;

        @Override
        public boolean equals(Object o) {
            if (o instanceof ConnectionInfo) {
                if (true == connectionId.equals(((ConnectionInfo) o).connectionId)) {
                    if (true == uri.equals(((ConnectionInfo) o).uri)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    /**
     * Constructor
     */
    public ProxyWsClient() {
        mLogger.trace("ProxyWsClient()");

        // HTTP Client
        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setTrustAll(true);
        httpClientOptions.setSsl(true);
        httpClientOptions.setTryUseCompression(true);
        httpClientOptions.setMaxPoolSize(MAX_CLIENT_COUNT + 1);
        mHttpClient = Vertx.vertx().createHttpClient(httpClientOptions);

        // Connection Data
        mConnectedData = new ConcurrentHashMap<>();
        mConnectingData = new ConcurrentHashMap<>();
        mConnectionLock = new Object();

        // Reconnect Timer
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    reconnect();
                } catch (Exception e) {
                    // Nothing
                }
            }
        }, 1000, 2000);
    }

    /**
     * Connect
     */
    public String connect(ArrayList<String> uriList, MultiMap headers) {
        String connectionId = UUID.randomUUID().toString();
        ConcurrentHashSet connectingInfoSet = new ConcurrentHashSet();

        // Set connection info
        uriList.forEach(uri -> {
            ConnectionInfo connectionInfo = new ConnectionInfo();
            connectionInfo.connectionId = connectionId;
            connectionInfo.uri = uri;
            connectionInfo.headers = headers;
            connectingInfoSet.add(connectionInfo);
        });
        mConnectingData.put(connectionId, connectingInfoSet);
        return connectionId;
    }

    /**
     * Disconnect
     */
    public void disconnect(String connectionId) {
        ConcurrentHashSet<ConnectionInfo> connectedInfoSet = null;
        synchronized (mConnectionLock) {
            mConnectingData.remove(connectionId);
            connectedInfoSet = mConnectedData.remove(connectionId);
        }
        if (connectedInfoSet != null) {
            connectedInfoSet.forEach(connectionInfo -> {
                try {
                    if (connectionInfo.webSocket != null) {
                        connectionInfo.webSocket.close();
                    }
                } catch (Exception e) {
                    // Nothing
                }
            });
        }
    }

    /**
     * Send text
     */
    public void sendText(String connectionId, String text) {
        ConcurrentHashSet<ConnectionInfo> connectedInfoSet = mConnectedData.get(connectionId);
        if (connectedInfoSet != null) {
            connectedInfoSet.forEach(connectionInfo -> {
                if (connectionInfo.webSocket != null) {
                    connectionInfo.webSocket.writeFinalTextFrame(text);
                }
            });
        }
    }

    /**
     * Reconnect
     */
    private void reconnect() {
        mConnectingData.values().forEach(connectionInfoSet -> {
            connectionInfoSet.forEach(connectionInfo -> {
                startToConnect(connectionInfo);
            });
        });
    }

    /**
     * Start to connect
     */
    private void startToConnect(final ConnectionInfo connectionInfo) {
        final String connectionId = connectionInfo.connectionId;

        // URI
        URI uri;
        try {
            uri = new URI(connectionInfo.uri);
        } catch (Exception e) {
            mLogger.error("Error occurred", e);
            return;
        }

        // Options
        RequestOptions options = new RequestOptions();
        if (uri.getScheme().equalsIgnoreCase("wss")) {
            options.setSsl(true);
        }
        options.setURI(connectionInfo.uri);
        options.setPort(uri.getPort());
        options.setHost(uri.getHost());


        // Headers
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        headers.addAll(connectionInfo.headers);

        // Headers (Need not)
        headers.remove("Connection");
        headers.remove("Host");
        headers.remove("Origin");
        headers.remove(HttpHeaderNames.SEC_WEBSOCKET_VERSION);
        headers.remove(HttpHeaderNames.SEC_WEBSOCKET_KEY);
        headers.remove(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS);

        // Log
        if (mLogger.isTraceEnabled() == true) {
            mLogger.trace("------------------------------------------------------------------------");
            mLogger.trace("WebSocket Request Headers (Proxy)");
            mLogger.trace("------------------------------------------------------------------------");
            headers.forEach(header -> mLogger.trace(header.getKey() + " = " + header.getValue()));
            mLogger.trace("------------------------------------------------------------------------");
        }

        // Connect
        mHttpClient.websocket(options, headers, new Handler<WebSocket>() {
            @Override
            public void handle(WebSocket websocket) {
                // Connected
                synchronized (mConnectionLock) {
                    ConcurrentHashSet<ConnectionInfo> connectedInfoSet = mConnectedData.get(connectionId);
                    if (connectedInfoSet == null) {
                        connectedInfoSet = new ConcurrentHashSet<>();
                        mConnectedData.put(connectionId, connectedInfoSet);
                    } else {
                        if (connectedInfoSet.contains(connectionInfo) == true) {
                            websocket.close();
                            return;
                        }
                    }
                    connectedInfoSet.add(connectionInfo);
                    connectionInfo.webSocket = websocket;
                    // Remove from connecting
                    ConcurrentHashSet<ConnectionInfo> connectingInfoSet = mConnectingData.get(connectionId);
                    if (connectingInfoSet != null) {
                        if (connectingInfoSet.contains(connectionInfo) == true) {
                            connectingInfoSet.remove(connectionInfo);
                        }
                    }
                }

                // Handler (Frame)
                websocket.frameHandler(new Handler<WebSocketFrame>() {
                    @Override
                    public void handle(WebSocketFrame frame) {
                        mLogger.trace("Frame Data : " + frame.textData());
                    }
                });

                // Handler (End)
                websocket.drainHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        mLogger.trace("End");
                    }
                });

                // Handler (End)
                websocket.endHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        mLogger.trace("End");
                    }
                });

                // Handler (Close)
                websocket.closeHandler(new Handler<Void>() {
                    @Override
                    public void handle(Void event) {
                        mLogger.info("Closed # " + connectionInfo.uri);
                        // Reconnect
                        synchronized (mConnectionLock) {
                            ConcurrentHashSet<ConnectionInfo> connectedInfoSet = mConnectedData.get(connectionId);
                            if (connectedInfoSet != null) {
                                if (connectedInfoSet.contains(connectionInfo) == true) {
                                    connectedInfoSet.remove(connectionInfo);
                                    ConcurrentHashSet<ConnectionInfo> connectingInfoSet = mConnectingData.get(connectionId);
                                    if (connectingInfoSet == null) {
                                        connectingInfoSet = new ConcurrentHashSet<>();
                                        mConnectingData.put(connectionId, connectingInfoSet);
                                    }
                                    connectingInfoSet.add(connectionInfo);
                                }
                            }
                        }
                    }
                });

                // Handler (Exception)
                websocket.exceptionHandler(new Handler<Throwable>() {
                    @Override
                    public void handle(Throwable event) {
                        mLogger.error("Exception # " + connectionInfo.uri);
                        mLogger.error("Exception # " + event);
                    }
                });
            }
        });
    }


}