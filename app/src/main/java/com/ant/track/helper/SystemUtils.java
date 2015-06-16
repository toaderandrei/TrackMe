package com.ant.track.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import com.ant.track.application.GPSLiveTrackerApplication;

/**
 * Created by Toader on 6/3/2015.
 */
public class SystemUtils {


    private static final String TAG = SystemUtils.class.getSimpleName();

    /**
     * Tries to acquire a partial wake lock if not already acquired.
     * Acquire a wake lock if not already acquired.
     *
     * @param context  the context
     * @param wakeLock wake lock or null
     */
    @SuppressLint("Wakelock")
    public static PowerManager.WakeLock acquireWakeLock(Context context, PowerManager.WakeLock wakeLock) {
        Log.i(TAG, "Acquiring wake lock.");
        try {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager == null) {
                Log.e(TAG, "Power manager null.");
                return wakeLock;
            }
            if (wakeLock == null) {
                wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
                if (wakeLock == null) {
                    Log.e(TAG, "Cannot create a new wake lock.");
                    return null;
                }
            }
            if (!wakeLock.isHeld()) {
                wakeLock.acquire();
                if (!wakeLock.isHeld()) {
                    Log.e(TAG, "Cannot acquire wake lock.");
                }
            }
        } catch (RuntimeException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return wakeLock;
    }


    /**
     * gets the device id of the current android device.
     *
     * @return the deviceid.
     */
    public static String getDeviceId() {
        return Settings.Secure.getString(GPSLiveTrackerApplication.getInstance().getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    /**
     * gets the mac address of the current device.
     *
     * @return the mac address.
     */
    public static String getMacAddress() {
        WifiManager wifiMan = (WifiManager) GPSLiveTrackerApplication.getInstance().getApplicationContext().getSystemService(
                Context.WIFI_SERVICE);
        WifiInfo wifiInf = wifiMan.getConnectionInfo();
        String macAddr = wifiInf.getMacAddress();
        return macAddr;
    }

}