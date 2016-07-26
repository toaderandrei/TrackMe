package com.ant.track.lib.db.content.datasource;

import android.content.SharedPreferences;

import com.ant.track.lib.db.content.publisher.RouteDataSourceListener;

/**
 * Observer for preference changes.
 */
public class PreferenceSourceObserverImpl implements SharedPreferences.OnSharedPreferenceChangeListener {

    private RouteDataSourceListener dataSourceListener;

    public PreferenceSourceObserverImpl(RouteDataSourceListener listener) {
        this.dataSourceListener = listener;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        dataSourceListener.notifyPreferenceChanged(key);
    }

}
