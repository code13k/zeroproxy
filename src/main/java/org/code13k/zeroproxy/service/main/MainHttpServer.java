package org.code13k.zeroproxy.service.main;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.*;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang3.StringUtils;
import org.code13k.zeroproxy.business.proxy.http.ProxyHttpManager;
import org.code13k.zeroproxy.config.AppConfig;
import org.code13k.zeroproxy.config.ProxyConfig;
import org.code13k.zeroproxy.model.ProxyResponse;
import org.code13k.zeroproxy.model.config.proxy.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.function.Consumer;


public class MainHttpServer extends AbstractVerticle {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(MainHttpServer.class);

    // Const
    public static final int PORT = AppConfig.getInstance().getPort().getMainHttp();


    /**
     * start()
     */
    @Override
    public void start() throws Exception {
        super.start();
        mLogger.trace("start()");

        // Init
        HttpServerOptions httpServerOptions = new HttpServerOptions();
        httpServerOptions.setCompressionSupported(true);
        httpServerOptions.setPort(PORT);
        httpServerOptions.setIdleTimeout(10); // seconds
        HttpServer httpServer = vertx.createHttpServer(httpServerOptions);

        // Routing
        ArrayList<ProxyInfo> channelList = ProxyConfig.getInstance().getChannelList();
        Router router = Router.router(vertx);
        for (int index = 0; index < channelList.size(); index++) {
            ProxyInfo channel = channelList.get(index);

            // Make route path
            String location = channel.getLocation();
            if (location.length() == 1) {
                location = location + "*";
            } else {
                if ('/' == location.charAt(location.length() - 1)) {
                    location = location + "*";
                } else if ('*' != location.charAt(location.length() - 1)) {
                    location = location + "/*";
                }
            }
            final String routePath = location;
            final int channelIndex = index;
            mLogger.trace("routePath=" + routePath);
            mLogger.trace("channelIndex=" + channelIndex);

            // Set Route
            router.route(routePath).handler(routingContext -> {
                // Log
                mLogger.debug("ROUTE-PATH # " + routePath);
                mLogger.debug("CHANNEL-INDEX # " + channelIndex);

                // Init
                final ProxyInfo proxyInfo = ProxyConfig.getInstance().getChannel(channelIndex);
                final HttpMethod requestMethod = routingContext.request().method();
                final MultiMap requestHeaders = routingContext.request().headers();
                final String requestUri = routingContext.request().uri();
                final String pathString = parsePath(requestUri, proxyInfo.getLocation());

                // Log
                if (mLogger.isTraceEnabled() == true) {
                    mLogger.trace("pathString = " + pathString);
                    mLogger.trace("------------------------------------------------------------------------");
                    mLogger.trace("Origin Request Headers");
                    mLogger.trace("------------------------------------------------------------------------");
                    mLogger.trace("METHOD # " + requestMethod);
                    mLogger.trace("URL # " + requestUri);
                    requestHeaders.forEach(header -> mLogger.trace(header.getKey() + " = " + header.getValue()));
                    mLogger.trace("------------------------------------------------------------------------");
                }

                // Settings
                if (routingContext.request().method() == HttpMethod.POST) {
                    routingContext.request().setExpectMultipart(true);
                }

                // Processing
                routingContext.request().bodyHandler(new Handler<Buffer>() {
                    @Override
                    public void handle(Buffer body) {
                        mLogger.trace("BODY # " + body.toString("UTF-8"));

                        ProxyHttpManager.getInstance().proxy(channelIndex, pathString, requestMethod, requestHeaders, body, new Consumer<ProxyResponse>() {
                            @Override
                            public void accept(ProxyResponse proxyResponse) {
                                try {
                                    if (routingContext.response().closed() == true) {
                                        mLogger.info("Response is closed # " + routingContext.response().toString());
                                    } else if (routingContext.response().ended() == true) {
                                        mLogger.info("Response is ended # " + routingContext.response().toString());
                                    } else {
                                        if (proxyResponse == null) {
                                            sendResponse(routingContext, 502, "Bad Gateway");
                                        } else {
                                            routingContext.response().headers().addAll(proxyResponse.getHeaders());
                                            routingContext.response().setStatusCode(proxyResponse.getStatusCode());
                                            routingContext.response().setStatusMessage(proxyResponse.getStatusMessage());
                                            if (proxyResponse.getBody() == null) {
                                                routingContext.response().end();
                                            } else {
                                                routingContext.response().end(proxyResponse.getBody());
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    mLogger.error("Error occurred", e);
                                }
                            }
                        });
                    }
                });
            });
        }

        // Listen
        httpServer.requestHandler(router::accept).listen();

        // End
        logging(httpServerOptions, router);
    }

    /**
     * Logging
     */
    private void logging(HttpServerOptions httpServerOptions, Router router) {
        synchronized (mLogger) {
            // Begin
            mLogger.info("------------------------------------------------------------------------");
            mLogger.info("Main HTTP Server");
            mLogger.info("------------------------------------------------------------------------");

            // Vert.x
            mLogger.info("Vert.x clustered = " + getVertx().isClustered());
            mLogger.info("Vert.x deployment ID = " + deploymentID());

            // Http Server Options
            mLogger.info("Port = " + httpServerOptions.getPort());
            mLogger.info("Idle timeout (second) = " + httpServerOptions.getIdleTimeout());
            mLogger.info("Compression supported = " + httpServerOptions.isCompressionSupported());
            mLogger.info("Compression level = " + httpServerOptions.getCompressionLevel());

            // Route
            router.getRoutes().forEach(r -> {
                mLogger.info("Routing path = " + r.getPath());
            });

            // End
            mLogger.info("------------------------------------------------------------------------");
        }
    }

    /**
     * Send response
     */
    public static void sendResponse(RoutingContext routingContext, int statusCode, String statusMessage) {
        routingContext.response().putHeader(HttpHeaderNames.CONTENT_TYPE, "text/plain");
        routingContext.response().setStatusCode(statusCode).setStatusMessage(statusMessage).end(statusMessage);
    }

    /**
     * Parse path
     */
    public static String parsePath(String requestUrl, String location) {
        if (StringUtils.isEmpty(requestUrl) == true) {
            return null;
        }
        if (StringUtils.isEmpty(location) == true) {
            return null;
        }
        StringBuffer pathPrefixBuffer = new StringBuffer();
        pathPrefixBuffer.append(location);
        if (location.charAt(location.length() - 1) != '/') {
            pathPrefixBuffer.append("/");
        }
        String pathPrefix = pathPrefixBuffer.toString();
        int index = StringUtils.indexOf(requestUrl, pathPrefix);
        String path = StringUtils.substring(requestUrl, index + pathPrefix.length());
        return path;
    }
}
