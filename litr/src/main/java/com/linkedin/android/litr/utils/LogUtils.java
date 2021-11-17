package com.linkedin.android.litr.utils;

import android.util.Log;

public class LogUtils {
    public static LitrLogger instance = new LitrLogger() {
        @Override
        public void e(String tag, String message, Throwable error) {
            Log.e(tag, message, error);
        }

        @Override
        public void d(String tag, String message) {
            Log.d(tag, message);
        }
    };

    public static void e(String tag, String message, Throwable error) {
        instance.e(tag, message, error);
    }

    public static void e(String tag, String message) {
        instance.e(tag, message, null);
    }

    public static void i(String tag, String message) {
        instance.d(tag, message);
    }

    public static void d(String tag, String message) {
        instance.d(tag, message);
    }
}
