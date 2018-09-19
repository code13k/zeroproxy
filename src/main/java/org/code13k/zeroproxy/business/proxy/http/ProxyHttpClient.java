package org.code13k.zeroproxy.business.proxy.http;

import com.google.gson.GsonBuilder;
import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import org.apache.commons.lang3.StringUtils;
import org.code13k.zeroproxy.app.Env;
import org.code13k.zeroproxy.config.ProxyConfig;
import org.code13k.zeroproxy.model.ProxyResponse;
import org.code13k.zeroproxy.model.config.proxy.ProxyInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ProxyHttpClient {
    // Logger
    private static final Logger mLogger = LoggerFactory.getLogger(ProxyHttpClient.class);

    // Data
    private WebClient mWebClient;
    private ProxyInfo mProxyInfo;
    private int mRoundRobinIndex = 0;

    /**
     * Constructor
     */
    public ProxyHttpClient(ProxyInfo proxyInfo, int eventLoopPoolSize) {
        mProxyInfo = proxyInfo;
        mLogger.trace("ProxyHttpClient() # " + mProxyInfo);

        // Set user-agent
        String userAgent = "Code13k-ZeroProxy/" + Env.getInstance().getVersionString();
        mLogger.trace("User-Agent = " + userAgent);

        // WebClientOptions
        WebClientOptions webClientOptions = new WebClientOptions();
        webClientOptions.setUserAgent(userAgent);
        webClientOptions.setTrustAll(true);
        webClientOptions.setSsl(true);
        webClientOptions.setTryUseCompression(true);
        webClientOptions.setConnectTimeout(mProxyInfo.getConnectTimeout());
        webClientOptions.setIdleTimeout(mProxyInfo.getIdleTimeout());

        // Init VertxOptions
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setEventLoopPoolSize(eventLoopPoolSize);

        // Create WebClient
        mWebClient = WebClient.create(Vertx.vertx(vertxOptions), webClientOptions);
    }

    /**
     * Proxy
     */
    public void proxy(String originPath, HttpMethod originMethod, MultiMap originHeaders, Buffer originBody, Consumer<ProxyResponse> consumer) {
        // Headers
        final MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        headers.addAll(originHeaders);

        // Method
        final HttpMethod method = (originMethod == null) ? HttpMethod.GET : originMethod;

        // Body
        Buffer body = originBody;

        // URI
        ArrayList<String> uriList = getTargetURI(originPath);
        mLogger.trace("uriList = " + uriList);

        // Request
        final int targetCount = uriList.size();
        AtomicInteger processingCount = new AtomicInteger(targetCount);
        ArrayList<Map> result = new ArrayList<>();
        uriList.forEach(uri -> {
            requestTarget(uri, method, headers, body, new Consumer<HttpResponse<Buffer>>() {
                @Override
                public void accept(HttpResponse<Buffer> response) {
                    /**
                     * Single Request
                     */
                    if (targetCount == 1) {
                        ProxyResponse proxyResponse = new ProxyResponse();
                        if (response == null) {
                            proxyResponse.setStatusCode(504);
                            proxyResponse.setStatusMessage("Gateway Time-out");
                            proxyResponse.setHeaders(makeDefaultHeaders());
                            proxyResponse.setBody(null);
                        } else {
                            proxyResponse.setStatusCode(response.statusCode());
                            proxyResponse.setStatusMessage(response.statusMessage());
                            proxyResponse.setHeaders(response.headers());
                            proxyResponse.setBody(response.body());
                        }
                        consumer.accept(proxyResponse);
                    }

                    /**
                     * Multi Request
                     */
                    else {
                        Map<String, Object> resultItem = new HashMap<>();
                        if (response == null) {
                            resultItem.put("uri", uri);
                            resultItem.put("statusCode", 504);
                            resultItem.put("statusMessage", "Gateway Time-out");
                            resultItem.put("headers", makeDefaultHeaders());
                            resultItem.put("body", "");
                        } else {
                            resultItem.put("uri", uri);
                            resultItem.put("statusCode", response.statusCode());
                            resultItem.put("statusMessage", response.statusMessage());
                            resultItem.put("headers", convertHeaders(response.headers()));
                            resultItem.put("body", response.bodyAsString());
                        }
                        result.add(resultItem);

                        // END
                        if (processingCount.decrementAndGet() == 0) {
                            Buffer proxyBody = Buffer.buffer(new GsonBuilder().create().toJson(result));
                            ProxyResponse proxyResponse = new ProxyResponse();
                            proxyResponse.setStatusCode(200);
                            proxyResponse.setStatusMessage("OK");
                            proxyResponse.setHeaders(makeProxyResponseHeaders());
                            proxyResponse.setBody(proxyBody);
                            consumer.accept(proxyResponse);
                        }
                    }
                }
            });
        });
    }

    /**
     * Convert MultiMap to HashMap
     */
    private HashMap<String, String> convertHeaders(MultiMap headers) {
        HashMap<String, String> result = new HashMap<>();
        if (headers != null) {
            headers.forEach(header -> {
                result.put(header.getKey(), header.getValue());
            });
        }
        return result;
    }

    /**
     * Make default headers
     */
    private MultiMap makeDefaultHeaders() {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain");
        return headers;
    }

    /**
     * Make response headers for multi proxy
     */
    private MultiMap makeProxyResponseHeaders() {
        MultiMap headers = MultiMap.caseInsensitiveMultiMap();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        return headers;
    }

    /**
     * Get target URI
     */
    private ArrayList<String> getTargetURI(String originPath) {
        ArrayList<String> result = new ArrayList<>();
        ArrayList<String> uriList = new ArrayList<>();
        mProxyInfo.getTargets().forEach(targetBaseUri -> {
            String uri = targetBaseUri + "/" + originPath;
            uriList.add(uri);
        });
        if (mProxyInfo.getType().equalsIgnoreCase(ProxyConfig.ChannelType.ROUND_ROBIN)) {
            String uri = uriList.get(mRoundRobinIndex);
            mRoundRobinIndex = (mRoundRobinIndex + 1) % mProxyInfo.getTargets().size();
            result.add(uri);
        } else if (mProxyInfo.getType().equalsIgnoreCase(ProxyConfig.ChannelType.RANDOM)) {
            int index = (int) (System.nanoTime() % mProxyInfo.getTargets().size());
            mLogger.trace("index = " + index);
            result.add(mProxyInfo.getTargets().get(index));
        } else if (mProxyInfo.getType().equalsIgnoreCase(ProxyConfig.ChannelType.ALL)) {
            result.addAll(uriList);
        }
        return result;
    }

    /**
     * Request target
     */
    private void requestTarget(String uri, HttpMethod method, MultiMap headers, Buffer body, Consumer<HttpResponse<Buffer>> consumer) {
        HttpRequest<Buffer> request = mWebClient.requestAbs(method, uri);

        // Headers
        headers.forEach(header -> {
            request.putHeader(header.getKey(), header.getValue());
        });

        // Header (Host)
        try {
            String host = request.headers().get("Host");
            if (StringUtils.isEmpty(host) == false) {
                URL url = new URL(uri);
                if (url.getPort() > 0) {
                    host = url.getHost() + ":" + url.getPort();
                } else {
                    host = url.getHost();
                }
                request.headers().remove("Host");
                request.headers().add("Host", host);
            }
        } catch (Exception e) {
            request.headers().remove("Host");
            mLogger.error("Error occurred when parse target uri", e);
        }

        // Header (Referer)
        request.headers().remove("Referer");

        // Log
        if (mLogger.isTraceEnabled() == true) {
            mLogger.trace("------------------------------------------------------------------------");
            mLogger.trace("Request Headers");
            mLogger.trace("------------------------------------------------------------------------");
            mLogger.trace("URI # " + uri);
            mLogger.trace("METHOD # " + method);
            request.headers().forEach(header -> mLogger.trace(header.getKey() + " = " + header.getValue()));
            mLogger.trace("------------------------------------------------------------------------");
        }

        // Send
        request.sendBuffer(body, new Handler<AsyncResult<HttpResponse<Buffer>>>() {
            @Override
            public void handle(AsyncResult<HttpResponse<Buffer>> targetResponseResult) {
                // Log
                if (mLogger.isTraceEnabled() == true) {
                    mLogger.trace("------------------------------------------------------------------------");
                    mLogger.trace("Response Result");
                    mLogger.trace("------------------------------------------------------------------------");
                    mLogger.trace("Result.succeeded # " + targetResponseResult.succeeded());
                    mLogger.trace("Result.failed # " + targetResponseResult.failed());
                    mLogger.trace("Result.cause # " + targetResponseResult.cause());
                    if (targetResponseResult.result() != null) {
                        mLogger.trace("Result.statusCode # " + targetResponseResult.result().statusCode());
                        mLogger.trace("Result.statusMessage # " + targetResponseResult.result().statusMessage());
                        mLogger.trace("------------------------------------------------------------------------");
                        mLogger.trace("Response Headers");
                        mLogger.trace("------------------------------------------------------------------------");
                        targetResponseResult.result().headers().forEach(header -> mLogger.trace(header.getKey() + " = " + header.getValue()));
                    }
                    mLogger.trace("------------------------------------------------------------------------");
                }

                // End
                consumer.accept(targetResponseResult.result());
            }
        });
    }
}
