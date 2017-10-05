package com.heyu.technologystack.network;

import android.text.TextUtils;

import com.heyu.technologystack.utils.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by heyu on 2017/8/31.
 */

public class HttpRequestImpl {

    public static final int DEFAULT_BUFFER_SIZE = 8 * 1024;
    public static final int DEFAULT_CONNECT_TIMEOUT = 1 * 500;
    public static final int DEFAULT_READ_TIMEOUT = 1 * 500;

    private final String DEFAULT_HTTP_ACCEPT_CHARSET = "UTF-8";
    private final String DEFAULT_HTTP_ACCEPT_LANGUAGE = "zh";
    private static final String SCHENE_NAME_HTTPS = "https";

    private final String LS = System.getProperties().getProperty("line.separator");
    private String mAcceptLanguage = DEFAULT_HTTP_ACCEPT_LANGUAGE;
    private String mAcceptCharset = DEFAULT_HTTP_ACCEPT_CHARSET;
    private int mBufferSize = DEFAULT_BUFFER_SIZE;
    private int mConnectTimeout = DEFAULT_CONNECT_TIMEOUT;
    private int mReadTimeout = DEFAULT_READ_TIMEOUT;
    private String TAG = "HttpRequestImpl";

    public HttpRequestImpl() {
    }

    public HttpResponseData send(HttpRequestParams httpRequestParams) throws IOException {
        HttpResponseData response = null;
        byte[] buffer = null;
        InputStream is = null;
        ByteArrayOutputStream baos = null;
        URL mUrl = null;
        HttpURLConnection huc = null;
        OutputStream os = null;
        try {
            mUrl = new URL(httpRequestParams.getUrl());
            if (SCHENE_NAME_HTTPS.equals(mUrl.getProtocol().toLowerCase())) {
                HttpsURLConnection https = (HttpsURLConnection) mUrl.openConnection();
                https.setSSLSocketFactory(httpRequestParams.getHttpsSSLFactory());
                huc = https;
            } else {
                huc = (HttpURLConnection) mUrl.openConnection();
            }
            huc.setChunkedStreamingMode(0);
            huc.setConnectTimeout(httpRequestParams.getConnectTimeOut() == 0 ? mConnectTimeout : httpRequestParams.getConnectTimeOut());
            huc.setReadTimeout(httpRequestParams.getReadTimeOut() == 0 ? mReadTimeout : httpRequestParams.getReadTimeOut());
            huc.setRequestMethod(httpRequestParams.getRequestMethod());
            huc.setDoInput(true);
            byte[] bytes = null;
            if (httpRequestParams.POST.equals(huc.getRequestMethod())) {
                huc.setDoOutput(true);
                bytes = httpRequestParams.getPostBody();
                if (bytes != null && bytes.length > 0) {
                    huc.setRequestProperty("Content-Length", String.valueOf(bytes.length));
                    os = huc.getOutputStream();
                    os.write(bytes);
                    os.flush();
                }
            }
            huc.setRequestProperty("Accept-Charset", TextUtils.isEmpty(httpRequestParams.getCharset()) ? mAcceptCharset : httpRequestParams.getCharset());
            huc.setRequestProperty("Accept-Language", TextUtils.isEmpty(httpRequestParams.getLanguage()) ? mAcceptLanguage : httpRequestParams.getLanguage());
            huc.setRequestProperty("Accept", "*/*");
            huc.setUseCaches(false);
            huc.setRequestProperty("Connection", "keep-alive");
            Map<String, String> requestProperties = httpRequestParams.getHeaders();
            if (requestProperties != null && !requestProperties.isEmpty()) {
                for (Map.Entry<String, String> entry : requestProperties.entrySet()) {
                    if (entry.getKey() != null) {
                        huc.setRequestProperty(entry.getKey(), entry.getValue());
                    }
                }
            }

            printRequestLog(huc, bytes);

            response = new HttpResponseData();
            int responseCode = huc.getResponseCode();
            response.setResponseCode(responseCode);
            byte[] result = null;
            if (HttpURLConnection.HTTP_BAD_REQUEST > responseCode) {
                is = huc.getInputStream();
                response.setInputStream(is);
                baos = new ByteArrayOutputStream();
                buffer = new byte[mBufferSize];
                int len = 0;
                while ((len = is.read(buffer, 0, mBufferSize)) != -1) {
                    baos.write(buffer, 0, len);
                }
                baos.flush();
                result = baos.toByteArray();
                response.setBytes(result);
                StringBuffer sb = new StringBuffer();
                InputStreamReader responseReader = new InputStreamReader(is, httpRequestParams.getCharset() == null ? mAcceptCharset : httpRequestParams.getCharset());
                BufferedReader reader = new BufferedReader(responseReader);
                String tempbf;
                while ((tempbf = reader.readLine()) != null) {
                    sb.append(tempbf);
                }
                response.setResult(new String(sb));
            }

            printResponseLog(huc, responseCode, result);
        } catch (Exception e) {
            if (e.getCause() instanceof SocketTimeoutException) {
                response.setResponseCode(HttpControlManager.NC_NETWORK_TIMEOUT);
            } else {
                response.setResponseCode(HttpControlManager.NC_REQUEST_SEND_FAILED);
            }
            e.printStackTrace();
        } finally {
            releaseAll(is, baos, huc, os);
        }
        return response;
    }

    private void releaseAll(InputStream is, ByteArrayOutputStream baos, HttpURLConnection huc, OutputStream os) throws IOException {
        byte[] buffer;
        URL mUrl;
        if (os != null) {
            os.close();
            os = null;
        }
        if (baos != null) {
            baos.close();
            baos = null;
        }
        if (is != null) {
            is.close();
            is = null;
        }
        if (huc != null) {
            huc.disconnect();
            huc = null;
        }
        buffer = null;
        mUrl = null;
    }

    private void printResponseLog(HttpURLConnection huc, int responseCode, byte[] result) {
        Map<String, List<String>> headerFields = huc.getHeaderFields();
        StringBuffer responseInfo = new StringBuffer();
        responseInfo.append("send.responseCode = ").append(responseCode).append(", url = ").append(huc.getURL().toString());
        if (!headerFields.isEmpty()) {
            responseInfo.append(LS);
        }
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            responseInfo.append("    ");
            if (entry.getKey() != null) {
                responseInfo.append(entry.getKey()).append(": ");
            }
            List<String> list = entry.getValue();
            for (String string : list) {
                responseInfo.append(string);
            }
            responseInfo.append(LS);
        }
        if (result != null) {
            responseInfo.append(LS);
            String resultContent = new String(result);
            if (headerFields != null && headerFields.containsKey("Content-Type")) {
                List<String> contentType = headerFields.get("Content-Type");
                for (String string : contentType) {
                    boolean isStartWith = false;
                    int len = 0;
                    if (string != null) {
                        isStartWith = !string.startsWith("text/");
                        len = string.length();
                    }
                    if (isStartWith || string == null || len == 0) {
                        resultContent = "[..................]";
                        break;
                    }
                }
            }
            responseInfo.append("result.length = " + formatSize(result.length) + ", result = " + resultContent);
        }
        Logger.d(responseInfo.toString());
        responseInfo.setLength(0);
    }

    private void printRequestLog(HttpURLConnection huc, byte[] bytes) throws UnsupportedEncodingException {
        StringBuffer requestInfo = new StringBuffer();
        requestInfo.append("send.url = ").append(huc.getURL().toString()).append(", requestMethod = ").append(huc.getRequestMethod()).append(", connectTimeout = ").append(huc.getConnectTimeout())
                .append(", readTimeout = ").append(huc.getReadTimeout());
        Map<String, List<String>> map = huc.getRequestProperties();
        if (!map.isEmpty()) {
            requestInfo.append(LS);
        }
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            requestInfo.append("    ");
            if (entry.getKey() != null) {
                requestInfo.append(entry.getKey()).append(": ");
            }
            List<String> list = entry.getValue();
            for (String string : list) {
                requestInfo.append(string);
            }
            requestInfo.append(LS);
        }
        if (bytes != null) {
            requestInfo.append(LS);
            requestInfo.append("bytes.length = " + formatSize(bytes.length) + ", bytes = " + (TextUtils.isEmpty(mAcceptCharset) ? new String(bytes) : new String(bytes, mAcceptCharset)));
        }
        Logger.d(requestInfo.toString());
        requestInfo.setLength(0);
    }


    public static String formatSize(long size) {
        float ONE_KB = 1024F;
        float ONE_MB = ONE_KB * ONE_KB;
        float ONE_GB = ONE_KB * ONE_MB;
        String displaySize;
        DecimalFormat df = new DecimalFormat("0.00");
        if (size >= ONE_KB && size < ONE_MB) {
            displaySize = String.valueOf(df.format(size / ONE_KB)) + " KB";
        } else if (size >= ONE_MB && size < ONE_GB) {
            displaySize = String.valueOf(df.format(size / ONE_MB)) + " MB";
        } else if (size >= ONE_GB) {
            displaySize = String.valueOf(df.format(size / ONE_GB)) + " GB";
        } else {
            displaySize = String.valueOf(df.format(size)) + " B";
        }
        return displaySize;
    }

}
