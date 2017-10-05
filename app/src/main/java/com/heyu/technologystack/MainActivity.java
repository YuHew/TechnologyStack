package com.heyu.technologystack;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.heyu.technologystack.network.HttpCallback;
import com.heyu.technologystack.network.HttpControlManager;
import com.heyu.technologystack.network.HttpRequestParams;
import com.heyu.technologystack.network.HttpResponseData;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Map<String, String> getParam = new HashMap<String, String>();
        String url = "";
        Map<String, String> header = new HashMap<String, String>();
        HttpRequestParams buildParams = new HttpRequestParams.Builder()
                .url(url)
                .requestMethod(HttpRequestParams.GET)
                .headers(header)
                .httpCallback(new HttpCallback<HttpResponseData>() {
                    @Override
                    public void onResponse(HttpResponseData httpResponseData) {
                        Log.d("MainActivity", "httpResponseData.getResponseCode():" + httpResponseData.getResponseCode());
                    }
                }).build();
        HttpControlManager.getInstance(this).sendHttpRequest(buildParams);
    }

    private static String prepareParam(Map<String, String> paramMap) {
        StringBuffer sb = new StringBuffer();
        if (paramMap == null) {
            return "";
        }
        if (paramMap.isEmpty()) {
            return "";
        } else {
            for (String key : paramMap.keySet()) {
                String value = paramMap.get(key);
                if (sb.length() < 1) {
                    sb.append(key).append("=").append(value);
                } else {
                    sb.append("&").append(key).append("=").append(value);
                }
            }
            return sb.toString();
        }
    }

}
