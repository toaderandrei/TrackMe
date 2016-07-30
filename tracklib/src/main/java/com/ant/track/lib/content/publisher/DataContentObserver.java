package com.ant.track.lib.content.publisher;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.net.Uri;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.constants.Constants;

/**
 * Class that handles the registration to the data provider.
 */
public class DataContentObserver {

    private final SharedPreferences sharedPreferences;

    public DataContentObserver() {
        sharedPreferences = getApp().getSharedPreferences(Constants.SETTINGS_NAME,Context.MODE_PRIVATE);
    }

    public void registerObserver(Uri uri, ContentObserver observer) {
        getContentResolver().registerContentObserver(uri, false, observer);
    }

    public ContentResolver getContentResolver() {
        return getApp().getContentResolver();
    }

    private TrackLibApplication getApp() {
        return TrackLibApplication.getInstance();
    }

    public void unregisterObserver(ContentObserver observer) {
        getContentResolver().unregisterContentObserver(observer);
    }

    /**
     * Registers a shared preference change listener.
     *
     * @param listener the listener
     */
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * Unregisters a shared preference change listener.
     *
     * @param listener the listener
     */
    public void unregisterOnSharedPreferenceChangeListener(
            SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
}
