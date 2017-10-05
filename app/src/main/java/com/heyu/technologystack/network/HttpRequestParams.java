package com.heyu.technologystack.network;

import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

/**
 * Created by heyu on 2017/8/31.
 */

public class HttpRequestParams {

    public static final String GET = "GET";
    public static final String POST = "POST";
    public static final String CHARSET_UTF8 = "UTF-8";
    public static final String CHARSET_GBK = "GBK";

    private String url;
    private String requestMethod;
    private String charset;
    private String language;
    private Map<String, String> headers;
    private Map<String, String> params;
    private int readTimeOut;
    private int connectTimeOut;
    private byte[] postBody;
    private HttpCallback<HttpResponseData> httpCallback;
    private SSLSocketFactory httpsSSLFactory;
    private int retryTimes;
    private int mRetryCountFlag;

    private HttpRequestParams(Builder builder) {
        url = builder.url;
        requestMethod = builder.requestMethod;
        retryTimes = builder.retryTimes;
        mRetryCountFlag = builder.mRetryCountFlag;
        charset = builder.charset;
        language = builder.language;
        headers = builder.headers;
        params = builder.params;
        readTimeOut = builder.readTimeOut;
        connectTimeOut = builder.connectTimeOut;
        postBody = builder.postBody;
        httpCallback = builder.httpCallback;
        httpsSSLFactory = builder.httpsSSLFactory;
    }

    public String getUrl() {
        return url;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public String getCharset() {
        return charset;
    }

    public String getLanguage() {
        return language;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public int getReadTimeOut() {
        return readTimeOut;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public byte[] getPostBody() {
        return postBody;
    }

    public HttpCallback<HttpResponseData> getHttpCallback() {
        return httpCallback;
    }

    public SSLSocketFactory getHttpsSSLFactory() {
        return httpsSSLFactory;
    }

    public int getRetryTimes() {
        return retryTimes;
    }

    public int getmRetryCountFlag() {
        return mRetryCountFlag;
    }

    public void tickRetryCount() {
        this.mRetryCountFlag++;
    }

    public boolean isCanRetry() {
        return mRetryCountFlag < retryTimes;
    }

    public static final class Builder {
        private String url;
        private String requestMethod;
        private int retryTimes;
        private int mRetryCountFlag;
        private String charset;
        private String language;
        private Map<String, String> headers;
        private Map<String, String> params;
        private int readTimeOut;
        private int connectTimeOut;
        private byte[] postBody;
        private HttpCallback<HttpResponseData> httpCallback;
        private SSLSocketFactory httpsSSLFactory;

        public Builder() {
        }

        public Builder url(String val) {
            url = val;
            return this;
        }

        public Builder requestMethod(String val) {
            requestMethod = val;
            return this;
        }

        public Builder retryTimes(int val) {
            retryTimes = val;
            return this;
        }

        public Builder mRetryCountFlag(int val) {
            mRetryCountFlag = val;
            return this;
        }

        public Builder charset(String val) {
            charset = val;
            return this;
        }

        public Builder language(String val) {
            language = val;
            return this;
        }

        public Builder headers(Map<String, String> val) {
            headers = val;
            return this;
        }

        public Builder params(Map<String, String> val) {
            params = val;
            return this;
        }

        public Builder readTimeOut(int val) {
            readTimeOut = val;
            return this;
        }

        public Builder connectTimeOut(int val) {
            connectTimeOut = val;
            return this;
        }

        public Builder postBody(byte[] val) {
            postBody = val;
            return this;
        }

        public Builder httpCallback(HttpCallback<HttpResponseData> val) {
            httpCallback = val;
            return this;
        }

        public Builder httpsSSLFactory(SSLSocketFactory val) {
            httpsSSLFactory = val;
            return this;
        }

        public HttpRequestParams build() {
            return new HttpRequestParams(this);
        }
    }
}
