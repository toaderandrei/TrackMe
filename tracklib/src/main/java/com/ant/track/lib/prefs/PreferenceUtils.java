package com.ant.track.lib.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.lib.service.RecordingStateUtils;

/**
 * Preference utils used for storing un-sensitive data.
 */
public class PreferenceUtils {

    public static final long DEFAULT_ROUTE_ID = -1L;
    //in meters
    public static final int RECORDING_GPS_ACCURACY_DEFAULT = 5;
    public static final int RECORDING_DISTANCE_DEFAULT = 15;
    public static final int DEFAULT_MAX_RECORDING_DISTANCE = 200;
    public static final RecordingState RECORDING_STATE_PAUSED_DEFAULT = RecordingState.PAUSED;
    public static final int DEFAULT_ROUTE_COLOR = android.R.color.holo_red_dark;
    public static int MAP_TYPE_DEFAUlT = 1;

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

    public static void setString(Context context, int keyId, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getKey(context, keyId), value);
        editor.apply();
    }

    public static boolean getBoolean(Context context, int keyId, boolean value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(getKey(context, keyId), value);
    }

    public static long getLong(Context context, int keyId) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(getKey(context, keyId), -1L);
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

    public static RecordingState getRecordingState(Context context, int recording_state_key, RecordingState recordingStatePausedDefault) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        String key = sharedPreferences.getString(getKey(context, recording_state_key), recordingStatePausedDefault.getState());
        RecordingState recordingState = RecordingStateUtils.getString(key);
        if (recordingState != null) {
            return recordingState;
        }
        return recordingStatePausedDefault;
    }

    public static void setRecordingState(Context context, int keyid, RecordingState state) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getKey(context, keyid), state.getState());
        editor.apply();
    }
}
