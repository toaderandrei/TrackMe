package com.ant.track.app.helper;

import android.os.Build;

import com.ant.track.app.widget.AppWidget;
import com.ant.track.app.widget.AppWidgetImpl;
import com.ant.track.app.widget.AppWidgetImplApi16;

/**
 *
 */
public class ApiHelper {

    public static boolean hasLocationMode() {
        boolean hasLocationServices;
        if (getSdkInt() >= 19) {
            hasLocationServices = true;
        } else if (getSdkInt() >= 17) {
            hasLocationServices = true;
        } else if (getSdkInt() >= 16) {
            hasLocationServices = true;
        } else if (getSdkInt() >= 14) {
            hasLocationServices = true;
        } else if (getSdkInt() >= 11) {
            hasLocationServices = false;
        } else if (getSdkInt() >= 10) {
            hasLocationServices = false;
        } else if (getSdkInt() >= 9) {
            hasLocationServices = false;
        } else {
            hasLocationServices = false;
        }
        return hasLocationServices;
    }

    private static int getSdkInt() {
        return Build.VERSION.SDK_INT;
    }

    public static boolean isApi23() {
        return Build.VERSION.SDK_INT == Build.VERSION_CODES.M;
    }

    public static AppWidget getAppWidget() {
        if (getSdkInt() >= 16) {
            return new AppWidgetImplApi16();
        }
        return new AppWidgetImpl();
    }
}
