package com.heyu.technologystack;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.heyu.technologystack.logger.CrashHandler;
import com.heyu.technologystack.logger.Logger;

public class BaseApplication extends Application {

    private static Context mContext;
    private static Handler mHandler;


    @Override
    public void onCreate() {
        super.onCreate();

        Logger.d("MusicApplication#onCreate");
        mContext = this;
        mHandler = new Handler(Looper.getMainLooper());
        Logger.updateDebuggableFlag(true);
        CrashHandler.getInstance().init(this);

    }
}
