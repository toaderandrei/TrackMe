package com.ant.track.lib.prefs;

import android.content.Context;
import android.content.SharedPreferences;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.lib.service.RecordingStateUtils;

/**
 * Preference utils used for storing un-sensitive data.
 */
public class PreferenceUtils {

    public static final long DEFAULT_ROUTE_ID = -1L;
    //in meters
    public static final int RECORDING_GPS_ACCURACY_DEFAULT = 50;
    public static final int RECORDING_DISTANCE_DEFAULT = 10;
    public static final int DEFAULT_MAX_RECORDING_DISTANCE = 200;
    public static final RecordingState RECORDING_STATE_NOT_STARTED_DEFAULT = RecordingState.NOT_STARTED;
    public static final int DEFAULT_ROUTE_COLOR = android.R.color.holo_red_dark;
    public static final Boolean RECORDING_LOCK_STATE_DEFAULT = false;
    public static int MAP_TYPE_DEFAULT = 1;


    // Track widget
    public static final int WIDGET_ITEM1_DEFAULT = 3; // moving time
    public static final int WIDGET_ITEM2_DEFAULT = 0; // distance
    public static final int WIDGET_ITEM3_DEFAULT = 1; // total time
    public static final int WIDGET_ITEM4_DEFAULT = 2; // average

    private PreferenceUtils() {

    }

    /**
     * Gets a preference key
     * * @param keyId the key id
     */
    public static String getKey(Context context, int keyId) {
        return context.getString(keyId);
    }

    public static void setInt(Context ctx, int keyId, int value) {
        Context context = getContext(ctx);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(getKey(context, keyId), value);
        editor.apply();
    }

    public static int getInt(Context ctx, int keyId, int value) {
        Context context = getContext(ctx);

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

    public static long getLong(Context ctx, int keyId) {
        Context context = getContext(ctx);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(getKey(context, keyId), -1L);
    }

    public static long getLong(Context ctx, int keyId, long defaultVal) {
        Context context = getContext(ctx);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        return sharedPreferences.getLong(getKey(context, keyId), defaultVal);
    }

    public static void setRouteId(Context ctx, int keyid, long routeId) {
        Context context = getContext(ctx);

        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(getKey(context, keyid), routeId);
        editor.apply();
    }

    public static SharedPreferences getSharedPrefs(Context context) {
        return context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
    }

    public static RecordingState getRecordingState(Context ctx, int recording_state_key, RecordingState recordingStatePausedDefault) {
        Context context = getContext(ctx);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        String key = sharedPreferences.getString(getKey(context, recording_state_key), recordingStatePausedDefault.getState());
        RecordingState recordingState = RecordingStateUtils.getString(key);
        if (recordingState != null) {
            return recordingState;
        }
        return recordingStatePausedDefault;
    }

    public static void setRecordingState(Context ctx, int keyid, RecordingState state) {
        Context context = getContext(ctx);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SETTINGS_KEY, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(getKey(context, keyid), state.getState());
        editor.apply();
    }

    private static Context getContext(Context ctx) {
        Context context = ctx;
        if (context == null) {
            context = TrackLibApplication.getInstance().getApplicationContext();
        }
        return context;
    }
}
