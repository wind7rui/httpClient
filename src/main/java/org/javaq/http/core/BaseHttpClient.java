package org.javaq.http.core;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.annotation.ThreadSafe;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.InitializingBean;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * based on Apache HttpClient4.4
 */
@ThreadSafe
public abstract class BaseHttpClient implements InitializingBean {

    private static final int DEFAULT_MAX_PER_ROUTE = 15;

    private static final int DEFAULT_MAX_TOTAL = 200;

    private static final int DEFAULT_TIME_TO_LIVE = 3000; //毫秒

    private CloseableHttpClient httpClient;

    private boolean releaseCon = false;

    private Integer maxPerRoute;

    private Integer maxTotal;

    private Integer timeToLive;

    private Integer defaultSocketTimeout = 5000;//milliseconds

    private Integer defaultConnectTimeout = 5000;//milliseconds

    private boolean retry = false;

    private Integer retryCount = 0;

    private SSLContextFactory sslContextFactory;

    public String get(String url) throws IOException {
        return this.get(url, -1, -1);
    }

    public String get(String url, int socketTimeout, int connectTimeout) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        return this.execute(httpGet, socketTimeout, connectTimeout);
    }

    public String postJson(String url, String json) throws IOException {
        return this.postJson(url, json, -1, -1);
    }

    public String postJson(String url, String json, int socketTimeout, int connectTimeout) throws IOException {
        StringEntity entity = new StringEntity(json, Consts.UTF_8);
        entity.setContentEncoding(ContentType.APPLICATION_JSON.getCharset().name());
        entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        return this.post(url, entity, socketTimeout, connectTimeout);
    }

    public String postJson(String url, String json, int socketTimeout, int connectTimeout, String charset) throws IOException {
        StringEntity entity = new StringEntity(json, charset);
        entity.setContentEncoding(ContentType.APPLICATION_JSON.getCharset().name());
        entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        return this.post(url, entity, socketTimeout, connectTimeout);
    }

    public String postXml(String url, String xml, int socketTimeout, int connectTimeout, String charset) throws IOException {
        StringEntity entity = new StringEntity(xml, charset);
        entity.setContentEncoding(ContentType.APPLICATION_XML.getCharset().name());
        entity.setContentType(ContentType.APPLICATION_XML.getMimeType());
        return this.post(url, entity, socketTimeout, connectTimeout, charset);
    }

    public String post(String url, HttpEntity entity) throws IOException {
        return this.post(url, entity, -1, -1);
    }

    public String post(String url, HttpEntity entity, int socketTimeout, int connectTimeout) throws IOException {
        HttpPost method = new HttpPost(url);
        method.setEntity(entity);
        return this.execute(method, socketTimeout, connectTimeout);
    }

    public String post(String url, HttpEntity entity, int socketTimeout, int connectTimeout, String charset) throws IOException {
        HttpPost method = new HttpPost(url);
        method.setEntity(entity);
        return this.execute(method, socketTimeout, connectTimeout, charset);
    }

    public String execute(HttpRequestBase request) throws IOException {
        return this.execute(request, -1, -1);
    }

    public String execute(HttpRequestBase request, int socketTimeout, int connectTimeout) throws IOException {
        return this.execute(request, socketTimeout, connectTimeout, "UTF-8");
    }

    public String execute(HttpRequestBase request, int socketTimeout, int connectTimeout, String charset) throws IOException {
        if (socketTimeout <= 0) {
            socketTimeout = this.defaultSocketTimeout;
        }
        if (connectTimeout <= 0) {
            connectTimeout = this.defaultConnectTimeout;
        }
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(socketTimeout)
                .setConnectTimeout(connectTimeout)
                .build();
        request.setConfig(requestConfig);
        try {
            return httpClient.execute(request, new StringResponseHandler(charset));
        } finally {
            if (releaseCon) {
                request.releaseConnection();
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (this.httpClient != null) {
            return;
        }

        int timeToLive;
        if (this.timeToLive != null) {
            timeToLive = this.timeToLive;
        } else {
            timeToLive = DEFAULT_TIME_TO_LIVE;
        }

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(getDefaultRegistry(), null, null, null, timeToLive, TimeUnit.MILLISECONDS);

        SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
        connectionManager.setDefaultSocketConfig(socketConfig);
        connectionManager.setValidateAfterInactivity(1000);

        if (this.maxPerRoute != null) {
            connectionManager.setDefaultMaxPerRoute(this.maxPerRoute);
        } else {
            connectionManager.setDefaultMaxPerRoute(DEFAULT_MAX_PER_ROUTE);
        }

        if (this.maxTotal != null) {
            connectionManager.setMaxTotal(this.maxTotal);
        } else {
            connectionManager.setMaxTotal(DEFAULT_MAX_TOTAL);
        }

        HttpClientBuilder clientBuilder = HttpClients.custom()
                .setRetryHandler(new DefaultHttpRequestRetryHandler(this.retryCount, this.retry));
        clientBuilder.setConnectionManager(connectionManager);
        this.httpClient = clientBuilder.build();
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpClient getHttpClient() {
        return this.httpClient;
    }

    public Integer getMaxPerRoute() {
        return maxPerRoute;
    }

    public void setMaxPerRoute(Integer maxPerRoute) {
        this.maxPerRoute = maxPerRoute;
    }

    public Integer getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(Integer maxTotal) {
        this.maxTotal = maxTotal;
    }

    public Integer getTimeToLive() {
        return timeToLive;
    }

    public void setTimeToLive(Integer timeToLive) {
        this.timeToLive = timeToLive;
    }

    public Integer getSocketTimeout() {
        return defaultSocketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.defaultSocketTimeout = socketTimeout;
    }

    public Integer getConnectTimeout() {
        return defaultConnectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.defaultConnectTimeout = connectTimeout;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public void setReleaseCon(boolean releaseCon) {
        this.releaseCon = releaseCon;
    }

    public SSLContextFactory getSslContextFactory() {
        return sslContextFactory;
    }

    public void setSslContextFactory(SSLContextFactory sslContextFactory) {
        this.sslContextFactory = sslContextFactory;
    }

    public static class StringResponseHandler extends BasicResponseHandler {
        private String charset = "UTF-8";

        public StringResponseHandler() {
        }

        public StringResponseHandler(String charset) {
            this.charset = charset;
        }

        @Override
        public String handleEntity(final HttpEntity entity) throws IOException {
            return EntityUtils.toString(entity, charset);
        }
    }

    private Registry<ConnectionSocketFactory> getDefaultRegistry() {
        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", getSSLConnectionSocketFactory())
                .build();
    }

    private SSLConnectionSocketFactory getSSLConnectionSocketFactory() {
        return null == sslContextFactory ? new SSLConnectionSocketFactory(SSLContexts.createDefault(), new NoopHostnameVerifier()) :
                new SSLConnectionSocketFactory(sslContextFactory.getSslContext(), sslContextFactory.getHostnameVerifier());
    }

}
