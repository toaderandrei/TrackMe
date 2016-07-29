package com.ant.track.app.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ant.track.app.R;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;
import com.google.android.gms.maps.GoogleMap;

/**
 * This is the main activity that it is started every time and it is in charge of showing distinct UI stuff, menus, and also here we initialize the content view.
 */
public class MainActivity extends ServiceConnectActivity {

    private long routeId;
    private long recordingRouteId = PreferenceUtils.DEFAULT_ROUTE_ID;

    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initializeToolbar();
        handleIntent(getIntent());
        sharedPreferences = getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.registerOnSharedPreferenceChangeListener(onSharedPreferencesListener);

        initRecordingAndServiceFragment();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setActionBarTitle();
    }

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferencesListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key != null) {
                if (TextUtils.equals(key, PreferenceUtils.getKey(MainActivity.this, R.string.route_id_key))) {
                    recordingRouteId = PreferenceUtils.getLong(MainActivity.this, R.string.route_id_key);
                }
                if (TextUtils.equals(key, PreferenceUtils.getKey(MainActivity.this, R.string.recording_state_key))) {
                    recordingState = PreferenceUtils.getRecordingState(MainActivity.this, R.string.recording_state_key, RecordingState.NOT_STARTED);
                }
            }
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null) {
            routeId = intent.getLongExtra(Constants.EXTRA_ROUTE_ID, -1L);
            if (routeId == -1L) {
                Toast.makeText(this, getString(R.string.first_start_app), Toast.LENGTH_SHORT).show();
            }
        } else {
            //clearly first start.
            Toast.makeText(this, getString(R.string.first_start_app), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        onUpdateUIControls(getRecordingState());
    }

    /**
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (getGoogleMap() != null) {
            int id;
            switch (getGoogleMap().getMapType()) {
                case GoogleMap.MAP_TYPE_NORMAL:
                    id = R.id.normal_view;
                    break;
                case GoogleMap.MAP_TYPE_SATELLITE:
                    id = R.id.satellite_view;
                    break;
                default:
                    id = R.id.normal_view;
                    break;
            }
            MenuItem item = menu.findItem(id);
            if (item != null) {
                item.setChecked(true);
            }
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int type;
        if (this.getGoogleMap() != null) {
            type = this.getGoogleMap().getMapType();
        }

        if (item.getItemId() == R.id.normal_view) {
            type = GoogleMap.MAP_TYPE_NORMAL;
        } else if (item.getItemId() == R.id.satellite_view) {
            type = GoogleMap.MAP_TYPE_SATELLITE;
        } else {
            return super.onOptionsItemSelected(item);
        }
        if (getGoogleMap() != null) {
            this.getGoogleMap().setMapType(type);
            item.setChecked(true);
        }
        return true;
    }

    protected GoogleMap getGoogleMap() {
        if (mMapFragment != null) {
            return mMapFragment.getGoogleMap();
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(onSharedPreferencesListener);
        sharedPreferences = null;
        super.onDestroy();
    }


    public RecordingState getRecordingState() {
        if (recordingState == RecordingState.NOT_STARTED) {
            recordingState = routeId == recordingRouteId ? RecordingState.STARTED : RecordingState.STOPPED;
        }
        return recordingState;
    }

    @Override
    public long getRouteId() {
        return routeId;
    }
}
