package com.ant.track.lib;

import android.content.Context;
import android.content.SharedPreferences;

import com.ant.track.lib.constants.Constants;

/**
 * Preference utils used for storing un-sensitive data.
 */
public class PreferenceUtils {

    public static final long DEFAULT_ROUTE_ID = -1L;

    private PreferenceUtils() {

    }

    /**
     * Gets a preference key
     * * @param keyId the key id
     */
    public static String getKey(Context context, int keyId) {
        return context.getString(keyId);
    }

    public static void setInt(Context context, int keyId, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getKey(context, keyId), value);
        editor.apply();
    }

    public static int getInt(Context context, int keyId, int value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(getKey(context, keyId), value);
    }


    public static void setBoolean(Context context, int keyId, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(getKey(context, keyId), value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, int keyId, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(getKey(context, keyId), value);
    }

    public static long getLong(Context context, int keyId, long defaultVal) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(getKey(context, keyId), defaultVal);
    }

    public static void setRouteId(Context context, int keyid, long routeId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(getKey(context, keyid), routeId);
        editor.apply();
    }

    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
    }
}
