package com.ant.track.app.helper;

import android.os.Build;

/**
 * Created by Toader on 6/2/2015.
 */
public class ApiHelper {

    public static boolean hasLocationMode() {
        boolean hasLocationServices = false;
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
}
