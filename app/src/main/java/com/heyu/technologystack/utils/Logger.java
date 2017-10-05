package com.heyu.technologystack.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by heyu on 2017/8/28.
 */

public class Logger {

    private static final String TAG = "OCTOPUS_CLIENT";

    private static final String LOG_PATH = Environment.getExternalStorageDirectory() + "/.heyu_dir";

    static String className;
    static String methodName;
    static int lineNumber;

    private static boolean misdebug = false;

    private Logger() {
        /* Protect from instantiations */
    }

    public static boolean isDebuggable() {
        return true;
    }

    private static String createLog(String log) {

        StringBuffer buffer = new StringBuffer();
        buffer.append("[");
        buffer.append(methodName);
        buffer.append(":");
        buffer.append(lineNumber);
        buffer.append("]");
        buffer.append(log);

        return buffer.toString();
    }

    private static void getMethodNames(StackTraceElement[] sElements) {
        className = sElements[1].getFileName();
        methodName = sElements[1].getMethodName();
        lineNumber = sElements[1].getLineNumber();
    }

    public static void e(String message) {
        if (!isDebuggable())
            return;

        // Throwable instance must be created before any methods
        getMethodNames(new Throwable().getStackTrace());
        Log.e(TAG, className + "--" + createLog(message));
    }

    public static void i(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.i(TAG, className + "--" + createLog(message));
    }

    public static void dSuper(String message) {

        getMethodNames(new Throwable().getStackTrace());
        Log.d(TAG, className + "--" + createLog(message));
    }

    public static void d(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.d(TAG, className + "--" + createLog(message));
    }

    public static void v(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.v(TAG, className + "--" + createLog(message));
    }

    public static void w(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.w(TAG, className + "--" + createLog(message));
    }

    public static void wtf(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.wtf(TAG, className + "--" + createLog(message));
    }

    public static void i2file(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.i(TAG, className + "--" + createLog(message));
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss", Locale.CHINA);
        String date = sDateFormat.format(new Date());
        wLog2File(date + "--" + className + "--" + createLog(message));
    }

    public static void d2file(String message) {
        if (!isDebuggable())
            return;

        getMethodNames(new Throwable().getStackTrace());
        Log.i(TAG, className + "--" + createLog(message));
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss", Locale.CHINA);
        String date = sDateFormat.format(new Date());
        wLog2File(date + "--" + className + "--" + createLog(message));
    }

    public static void d2fileSuper(String message) {
        if (!isDebuggable())
            return;
        getMethodNames(new Throwable().getStackTrace());
        Log.i(TAG, className + "--" + createLog(message));
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss", Locale.CHINA);
        String date = sDateFormat.format(new Date());
        wLog2File(date + "--" + className + "--" + createLog(message));
    }

    private static void wLog2File(String text) {
        if (!isDebuggable())
            return;
        File logFileDir = new File(LOG_PATH);
        if (!logFileDir.exists()) {
            logFileDir.mkdir();
        }
        SimpleDateFormat formatter = new SimpleDateFormat("yyyMMdd");
        Date curDate = new Date(System.currentTimeMillis());
        File logFile = new File(LOG_PATH + "/octopus_log_" + formatter.format(curDate) + "_.file");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
