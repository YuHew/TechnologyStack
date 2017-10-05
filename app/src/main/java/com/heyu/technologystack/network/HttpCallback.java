package com.heyu.technologystack.network;

/**
 * Created by heyu on 2017/8/31.
 */

public interface HttpCallback<Type> {
    void onResponse(Type type);
}
