package org.code13k.zeroproxy.service.proxy;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.code13k.zeroproxy.business.proxy.ws.ProxyWsManager;
import org.code13k.zeroproxy.config.AppConfig;
import org.code13k.zeroproxy.config.ProxyWsConfig;
import org.code13k.zeroproxy.model.config.proxy.ProxyWsInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class ProxyWsServer extends AbstractVerticle {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(ProxyWsServer.class);

    // Server Port
    public static final int PORT = AppConfig.getInstance().getPort().getProxyWs();

    /**
     * start()
     */
    @Override
    public void start() throws Exception {
        super.start();
        mLogger.info("start()");

        /**
         * Http Server
         */
        HttpServerOptions options = new HttpServerOptions();
        options.setCompressionSupported(true);
        HttpServer httpServer = vertx.createHttpServer(options);


        /**
         * Listen WebSocket
         */
        httpServer.websocketHandler(new Handler<ServerWebSocket>() {
            @Override
            public void handle(final ServerWebSocket ws) {
                // Log
                if (mLogger.isTraceEnabled() == true) {
                    mLogger.trace("------------------------------------------------------------------------");
                    mLogger.trace("WebSocket Request Headers");
                    mLogger.trace("------------------------------------------------------------------------");
                    mLogger.trace("PATH # " + ws.path());
                    ws.headers().forEach(header -> mLogger.trace(header.getKey() + " = " + header.getValue()));
                    mLogger.trace("------------------------------------------------------------------------");
                }

                // Route
                boolean tempRouteResult = false;
                int tempChannelIndex = -1;
                String tempPath = null;
                ArrayList<ProxyWsInfo> channelList = ProxyWsConfig.getInstance().getChannelList();
                for (int index = 0; index < channelList.size(); index++) {
                    final ProxyWsInfo channel = channelList.get(index);
                    final String routePath = makeRoutePath(channel.getLocation());
                    mLogger.trace("routePath = " + routePath);
                    if (ws.path().indexOf(routePath) == 0) {
                        tempChannelIndex = index;
                        tempPath = parsePath(ws.path(), routePath);
                        tempRouteResult = true;
                        break;
                    }
                }
                final boolean routeResult = tempRouteResult;
                final int channelIndex = tempChannelIndex;
                final String path = tempPath;
                mLogger.trace("routeResult = " + routeResult);
                mLogger.trace("channelIndex = " + channelIndex);
                mLogger.trace("path = " + path);

                // End
                if (routeResult == true) {
                    String connectionId = ProxyWsManager.getInstance().connect(channelIndex, path, ws.headers());
                    mLogger.debug("connectionId = " + connectionId);
                    handleMessage(ws, connectionId);
                } else {
                    ws.reject();
                }
            }
        }).listen(PORT);
    }

    /**
     * Handle message
     */
    protected void handleMessage(final ServerWebSocket ws, final String connectionId) {
        ws.frameHandler(new Handler<WebSocketFrame>() {
            @Override
            public void handle(WebSocketFrame event) {
                mLogger.trace("Frame Data : " + event.textData());
                ProxyWsManager.getInstance().sendText(connectionId, event.textData());
            }
        });

        ws.drainHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                mLogger.trace("drainHandler()");
            }
        });

        ws.endHandler(new Handler<Void>() {
            @Override
            public void handle(Void event) {
                mLogger.trace("endHandler()");
            }
        });

        ws.closeHandler(new Handler<Void>() {
            @Override
            public void handle(final Void event) {
                mLogger.trace("closeHandler()");
                ProxyWsManager.getInstance().disconnect(connectionId);
            }
        });

        ws.exceptionHandler(new Handler<Throwable>() {
            @Override
            public void handle(Throwable event) {
                mLogger.trace("exceptionHandler()", event);
            }
        });
    }

    /**
     * Make route path
     */
    private String makeRoutePath(String location) {
        String routePath = location;
        if (routePath.length() > 1) {
            if ('/' == routePath.charAt(routePath.length() - 1)) {
                routePath = routePath.substring(0, routePath.length() - 2);
            } else if ('*' == routePath.charAt(routePath.length() - 1)) {
                routePath = routePath.substring(0, routePath.length() - 3);
            }
        }
        return routePath;
    }

    /**
     * Parse path
     */
    private String parsePath(String requestPath, String routePath) {
        String result = requestPath.substring(routePath.length());
        if (StringUtils.isEmpty(result) == false) {
            if (result.charAt(0) == '/') {
                result = result.substring(1);
            }
        }
        return result;
    }
}