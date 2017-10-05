package com.heyu.technologystack.network;

import android.os.Process;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by heyu on 2017/8/31.
 */
public class PriorityThreadFactory implements ThreadFactory {
    public static final int THREAD_PRIORITY_DEFAULT_LESS = Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_LESS_FAVORABLE * 3;
    private final int mPriority;
    private final AtomicInteger mNumber = new AtomicInteger();
    private final String mName;

    public PriorityThreadFactory(String name) {
        mName = name;
        mPriority = THREAD_PRIORITY_DEFAULT_LESS;
    }

    public PriorityThreadFactory(String name, int priority) {
        mName = name;
        mPriority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        return new Thread(r, "PriorityThreadFactory-" + mName + '-' + mNumber.getAndIncrement()) {
            @Override
            public void run() {
                if ("ExecutorSocketRequest".equals(mName)) {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT + Process.THREAD_PRIORITY_MORE_FAVORABLE);
                } else if ("ExecutorUpload".equals(mName)) {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_DEFAULT);
                } else {
                    Process.setThreadPriority(mPriority);
                }
                super.run();
            }
        };
    }
}