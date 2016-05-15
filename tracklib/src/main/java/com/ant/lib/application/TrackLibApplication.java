package com.ant.lib.application;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

/**
 * Extension of the Android application.
 */
public class TrackLibApplication extends Application {

    protected static TrackLibApplication instance;

    private HandlerThread mHandlerThread;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        mHandlerThread = new HandlerThread("AppThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }


    public TrackLibApplication() {
        super();
        instance = this;
    }

    public static TrackLibApplication getInstance() {
        return instance;
    }

    public void runAsync(final Runnable runnable) {
        if (mHandler != null) {
            mHandler.post(runnable);
        } else {
            throw new IllegalStateException("handler of the application cannot be null!.");
        }
    }
}
