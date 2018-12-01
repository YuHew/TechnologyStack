package com.heyu.technologystack.network;

import android.content.Context;
import android.os.Process;

import com.heyu.technologystack.logger.Logger;
import com.heyu.technologystack.utils.NetworkUtils;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class HttpControlManager {

    private final HttpRequestImpl mHttpClient;
    private final int fMaxQueueSize = 512;
    private final CallableThreadPoolExecutor mExecutorHttpRequest;
    private boolean mManagerInited;
    private static HttpControlManager mNetManagerInstance = null;
    private Context mContext;

    private HttpControlManager(Context context) {
        mContext = context;
        mHttpClient = new HttpRequestImpl();

        mExecutorHttpRequest = new CallableThreadPoolExecutor(5, 5, 5L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory("ExecutorHttpRequest",
                Process.THREAD_PRIORITY_BACKGROUND),
                new RejectedExecutionHandler() {
                    @Override
                    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                        if (r instanceof FutureTask<?>) {
                            ((FutureTask<?>) r).cancel(true);
                        }
                        Logger.e("ExecutorHttpRequest.rejectedExecution.r = " + r);
                    }
                });
        mManagerInited = true;
    }

    public static HttpControlManager getInstance(Context context) {
        if (mNetManagerInstance == null) {
            mNetManagerInstance = new HttpControlManager(context);
        }
        return mNetManagerInstance;
    }

    private int getQueueSize() {
        int httpRequestCount = mExecutorHttpRequest.getActiveCount() + mExecutorHttpRequest.getQueue().size();
        Logger.d("httpRequestCount = " + httpRequestCount);
        return httpRequestCount;
    }

    private ConcurrentHashMap<String, FutureTask<HttpResponseData>> connectionPool = new ConcurrentHashMap();

    private class HttpRequestCallable implements Callable<HttpResponseData> {
        private final HttpRequestParams mRequest;

        private HttpRequestCallable(HttpRequestParams request) {
            mRequest = request;
        }

        @Override
        public HttpResponseData call() throws Exception {
            if (mManagerInited) {
                mRequest.tickRetryCount();
                return getHttpResult(mRequest);
            } else {
                return new HttpResponseData(NC_NETWORK_DISCONNECTED);
            }
        }
    }

    private class HttpRequestTask extends FutureTask<HttpResponseData> {
        private final HttpRequestCallable mCallable;

        /**
         * @param callable
         */
        private HttpRequestTask(Callable<HttpResponseData> callable) {
            super(callable);
            mCallable = (HttpRequestCallable) callable;
        }

        @Override
        protected void done() {
            try {
                HttpResponseData response = get();
                if (response != null && !isCancelled()) {
                    if (response.getResponseCode() == NC_NETWORK_TIMEOUT) {
                        if (mCallable.mRequest.isCanRetry()) {
                            sendHttpRequest(mCallable.mRequest);
                        } else {
                            if (mCallable.mRequest.getHttpCallback() != null) {
                                mCallable.mRequest.getHttpCallback().onResponse(response);
                            }
                        }
                        Logger.e("Timeout:" + mCallable.mRequest.isCanRetry());
                    } else {
                        if (mCallable.mRequest.getHttpCallback() != null) {
                            mCallable.mRequest.getHttpCallback().onResponse(response);
                        }
                    }
                }
            } catch (InterruptedException e) {
                Logger.e("InterruptedException:" + e.getMessage());
            } catch (ExecutionException e) {
                Logger.e("ExecutionException:" + e.getMessage());
            } catch (CancellationException e) {
                Logger.e("CancellationException:" + e.getMessage());
            } finally {
            }
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            cancel(NC_REQUEST_SEND_FAILED);
            return super.cancel(mayInterruptIfRunning);
        }

        private void cancel(int nativeStatusCode) {
            Logger.w("cancel.nativeStatusCode = " + nativeStatusCode + ", " + this);
            if (mCallable.mRequest.getHttpCallback() != null) {
                mCallable.mRequest.getHttpCallback().onResponse(new HttpResponseData(nativeStatusCode));
            }
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("HttpRequestTask [request = ").append(mCallable.mRequest);
            sb.append("]");
            return sb.toString();
        }
    }

    public void sendHttpRequest(HttpRequestParams httpRequest) {
        HttpRequestTask task = new HttpRequestTask(new HttpRequestCallable(httpRequest));
        if (NetworkUtils.isNetworkAvailable(mContext)) {
            if (getQueueSize() >= fMaxQueueSize) {
                task.cancel(NC_REQUEST_QUEUE_FULLED);
            } else {
                mExecutorHttpRequest.execute(task);
            }
        } else {
            task.cancel(NC_NETWORK_DISCONNECTED);
        }
    }

    private HttpResponseData getHttpResult(HttpRequestParams httpRequest) throws IOException {
        return mHttpClient.send(httpRequest);
    }

    public void destroy() {
        Logger.d("destroy");
        shutdownNow(mExecutorHttpRequest);
    }

    private void shutdownNow(ThreadPoolExecutor executor) {
        List<Runnable> list = executor.shutdownNow();
        Logger.d("shutdownNow.list.size() = " + (list == null ? 0 : list.size()));
        if (list != null) {
            for (Runnable runnable : list) {
                ((FutureTask<?>) runnable).cancel(true);
            }
            list.clear();
        }
    }

    /**
     * 本地返回码：网络响应超时
     */
    public static final int NC_NETWORK_TIMEOUT = -100;
    /**
     * 本地返回码：网络未连接
     */
    public static final int NC_NETWORK_DISCONNECTED = -101;
    /**
     * 本地返回码：Socket或者Http请求发送失败，包括IO异常、请求队列被取消的请求
     */
    public static final int NC_REQUEST_SEND_FAILED = -102;
    /**
     * 本地返回码：网络请求池已满
     */
    public static final int NC_REQUEST_QUEUE_FULLED = -104;

}
