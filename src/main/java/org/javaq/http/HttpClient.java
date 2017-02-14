package org.javaq.http;

import com.google.common.collect.ImmutableMap;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.javaq.http.core.BaseHttpClient;
import org.springframework.scheduling.annotation.Async;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpClient extends BaseHttpClient {


    /**
     * 异步发送get请求
     *
     * @param url the request url
     */
    @Async
    public void sendGetRequestByAsync(final String url) {
        try {
            super.get(url);
        } catch (IOException e) {
            //LOGGER.error("Fail to send get request", e);
        }
    }

    public String sendGetRequestAsString(String url) {
        try {
            return super.get(url);
        } catch (IOException e) {
            //LOGGER.error("Fail to send get request", e);
        }
        return null;
    }


    public String sendPostRequestAsString(String url) {
        return sendPostRequestAsString(url, -1, -1);
    }

    public String sendPostRequestAsString(String url, int socketTimeout, int connectTimeout) {
        HttpPost httpPost = new HttpPost(url);
        try {
            return super.execute(httpPost, socketTimeout, connectTimeout);
        } catch (IOException e) {
            //LOGGER.error(e, "Fail to send post request,message {}", e.getMessage());
        } finally {
            httpPost.abort();
        }
        return null;
    }

    public String sendPostRequestAsString(String url, ImmutableMap<String, String> params) {
        return sendPostRequestAsString(url, params, false);
    }

    public String sendJsonRequestByPost(RequestEntity requestEntity) throws IOException {
        return super.postJson(requestEntity.getUrl(), requestEntity.getJsonString(), requestEntity.getSocketTimeout(), requestEntity.getConnectTimeout(), requestEntity.getCharset());
    }

    public String sendXmlRequestByPost(RequestEntity requestEntity) throws IOException {
        StringEntity entity = new StringEntity(requestEntity.getXmlString(), requestEntity.getCharset());
        entity.setContentEncoding(ContentType.APPLICATION_XML.getCharset().name());
        entity.setContentType(ContentType.APPLICATION_XML.getMimeType());
        HttpPost httpPost = new HttpPost(requestEntity.getUrl());
        httpPost.setEntity(entity);
        if (!requestEntity.getHeaders().isEmpty()) {
            for (Header header : requestEntity.getHeaders()) {
                httpPost.addHeader(header);
            }
        }
        return super.execute(httpPost, requestEntity.getSocketTimeout(), requestEntity.getConnectTimeout(), requestEntity.getCharset());
    }

    public String sendFormRequestByPost(RequestEntity requestEntity) throws IOException {
        HttpPost httpPost = new HttpPost(requestEntity.getUrl());
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : requestEntity.getParams().entrySet()) {
            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8));
        try {
            return super.execute(httpPost, requestEntity.getSocketTimeout(), requestEntity.getConnectTimeout());
        } catch (IOException e) {
            //LOGGER.error(e, "Fail to send post request,message {}", e.getMessage());
            throw e;
        } finally {
            if (requestEntity.canAbort()) {
                httpPost.abort();
            }
        }
    }

    private String sendPostRequestAsString(String url, ImmutableMap<String, String> params, boolean abort) {
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> nameValuePairs = new ArrayList<>();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, Consts.UTF_8));
        try {
            return super.execute(httpPost);
        } catch (IOException e) {
            //LOGGER.error(e, "Fail to send post request,message {}", e.getMessage());
        } finally {
            if (abort) {
                httpPost.abort();
            }
        }
        return null;
    }

    public String sendPostRequestAsStringWithAbort(String url, ImmutableMap<String, String> params) {
        return this.sendPostRequestAsString(url, params, true);
    }

    public String sendPostJsonRequestAsString(String url, String jsonParam) {
        try {
            return super.postJson(url, jsonParam);
        } catch (IOException e) {
            //LOGGER.error("Fail to send post json request", e);
        }
        return null;
    }

    public String postJson(String url, String json, List<Header> headers) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        if (null != headers && !headers.isEmpty()) {
            for (Header header : headers) {
                httpPost.addHeader(header);
            }
        }
        StringEntity entity = new StringEntity(json, Consts.UTF_8);
        entity.setContentEncoding(ContentType.APPLICATION_JSON.getCharset().name());
        entity.setContentType(ContentType.APPLICATION_JSON.getMimeType());
        httpPost.setEntity(entity);
        return super.execute(httpPost);
    }

}
