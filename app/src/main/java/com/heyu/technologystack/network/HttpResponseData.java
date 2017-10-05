package com.heyu.technologystack.network;

import java.io.InputStream;

/**
 * Created by heyu on 2017/8/31.
 */

public class HttpResponseData {

    private int responseCode;
    private byte[] bytes;
    private int contentLength;
    private InputStream inputStream;
    private String result;

    public HttpResponseData() {
    }

    public HttpResponseData(int responseCode) {
        this.responseCode = responseCode;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
