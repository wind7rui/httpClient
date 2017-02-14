package org.javaq.http;


import com.google.common.collect.ImmutableMap;
import org.apache.http.Header;

import java.util.ArrayList;
import java.util.List;

public class RequestEntity {

    private String url;
    private ImmutableMap<String, String> params;
    private String jsonString;
    private String xmlString;
    private int socketTimeout = -1;//milliseconds
    private int connectTimeout = -1;//milliseconds
    private boolean canAbort = false;
    private String charset = "UTF-8";
    private List<Header> headers = new ArrayList<>();

    public String getUrl() {
        return url;
    }

    public ImmutableMap<String, String> getParams() {
        return params;
    }

    public int getSocketTimeout() {
        return socketTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public boolean canAbort() {
        return canAbort;
    }

    public String getCharset() {
        return charset;
    }

    public String getJsonString() {
        return jsonString;
    }

    public String getXmlString() {
        return xmlString;
    }

    public List<Header> getHeaders() {
        return headers;
    }

    public static final class Builder {
        private String url;
        private ImmutableMap<String, String> params;
        private int socketTimeout = -1;
        private int connectTimeout = -1;
        private boolean canAbort = false;
        private String charset = "UTF-8";
        private String jsonString;
        private String xmlString;
        private List<Header> headers = new ArrayList<>();

        private Builder() {
        }

        public static Builder aRequestEntity() {
            return new Builder();
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Builder withParams(ImmutableMap<String, String> params) {
            this.params = params;
            return this;
        }

        public Builder withSocketTimeout(int socketTimeout) {
            this.socketTimeout = socketTimeout;
            return this;
        }

        public Builder withConnectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }

        public Builder withCanAbort(boolean canAbort) {
            this.canAbort = canAbort;
            return this;
        }

        public Builder withCharset(String charset) {
            this.charset = charset;
            return this;
        }

        public Builder withJsonString(String jsonString) {
            this.jsonString = jsonString;
            return this;
        }

        public Builder withXmlString(String xmlString) {
            this.xmlString = xmlString;
            return this;
        }

        public Builder withHeaders(List<Header> headers) {
            this.headers = headers;
            return this;
        }

        public RequestEntity build() {
            RequestEntity requestEntity = new RequestEntity();
            requestEntity.canAbort = this.canAbort;
            requestEntity.params = this.params;
            requestEntity.connectTimeout = this.connectTimeout;
            requestEntity.url = this.url;
            requestEntity.socketTimeout = this.socketTimeout;
            requestEntity.charset = this.charset;
            requestEntity.jsonString = this.jsonString;
            requestEntity.xmlString = this.xmlString;
            requestEntity.headers = this.headers;
            return requestEntity;
        }
    }

    @Override
    public String toString() {
        return "RequestEntity{" +
                "url='" + url + '\'' +
                ", params=" + params +
                ", jsonString='" + jsonString + '\'' +
                ", xmlString='" + xmlString + '\'' +
                ", socketTimeout=" + socketTimeout +
                ", connectTimeout=" + connectTimeout +
                ", canAbort=" + canAbort +
                ", charset='" + charset + '\'' +
                ", headers=" + headers +
                '}';
    }
}
