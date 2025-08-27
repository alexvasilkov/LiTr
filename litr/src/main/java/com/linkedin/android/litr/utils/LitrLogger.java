package com.linkedin.android.litr.utils;

public interface LitrLogger {
    void e(String tag, String message, Throwable error);

    void d(String tag, String message);
}
