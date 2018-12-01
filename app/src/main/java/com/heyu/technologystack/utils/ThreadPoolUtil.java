package com.heyu.technologystack.utils;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadPoolUtil {

    private static int CORE_POOL_SIZE = 5;
    private static int MAX_POOL_SIZE = 100;
    private static int KEEP_ALIVE_TIME = 10000;
    private static BlockingQueue workQueue = new ArrayBlockingQueue(10);
    private static ThreadPoolExecutor threadPool;

    private ThreadPoolUtil() {
    }

    private static ThreadFactory threadFactory = new ThreadFactory() {

        private final AtomicInteger integer = new AtomicInteger();

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "myThreadPool thread:" + integer.getAndIncrement());
        }
    };

    static {
        threadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAX_POOL_SIZE, KEEP_ALIVE_TIME, TimeUnit.SECONDS, workQueue, threadFactory, new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable runnable, ThreadPoolExecutor threadPoolExecutor) {
                if(!threadPoolExecutor.isShutdown()){
                    threadPoolExecutor.getQueue().poll();
                    threadPoolExecutor.execute(runnable);
                }
            }
        });
    }

    public static void execute(Runnable runnable) {
        threadPool.execute(runnable);
    }

    public static void execute(FutureTask futureTask) {
        threadPool.execute(futureTask);
    }

    public static void cancel(FutureTask futureTask) {
        futureTask.cancel(true);
    }
}
