package com.ant.track.helper;

import android.os.Build;

/**
 * Created by Toader on 6/2/2015.
 */
public class ApiHelper {

    public static boolean hasLocationMode() {
        boolean hasLocationServices = false;
        if (Build.VERSION.SDK_INT >= 19) {
            hasLocationServices = true;
        } else if (Build.VERSION.SDK_INT >= 17) {
            hasLocationServices = true;
        } else if (Build.VERSION.SDK_INT >= 16) {
            hasLocationServices = true;
        } else if (Build.VERSION.SDK_INT >= 14) {
            hasLocationServices = true;
        } else if (Build.VERSION.SDK_INT >= 11) {
            hasLocationServices = false;
        } else if (Build.VERSION.SDK_INT >= 10) {
            hasLocationServices = false;
        } else if (Build.VERSION.SDK_INT >= 9) {
            hasLocationServices = false;
        } else {
            hasLocationServices = false;
        }
        return hasLocationServices;
    }
}
