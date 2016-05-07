package com.ant.track.application;

import android.app.Application;
import android.os.Handler;
import android.os.HandlerThread;

import com.ant.track.provider.DataProvider;
import com.ant.track.provider.IDataProvider;

/**
 * Created by Toader on 6/1/2015.
 */
public class GPSLiveTrackerApplication extends Application {


    private static GPSLiveTrackerApplication instance = null;
    Handler mHandler;
    HandlerThread mHandlerThread;

    public GPSLiveTrackerApplication() {
        super();
        GPSLiveTrackerApplication.instance = this;
    }

    public static GPSLiveTrackerApplication getInstance() {
        return GPSLiveTrackerApplication.instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandlerThread = new HandlerThread("AppThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    public IDataProvider getDataProvider() {
        return DataProvider.getInstance();
    }

    public void runAsync(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        mHandler.post(runnable);
    }
}
