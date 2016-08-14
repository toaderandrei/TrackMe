package com.ant.track.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.ant.track.app.R;
import com.ant.track.app.fragments.RecordControlsFragment;
import com.ant.track.app.service.utils.RecordingServiceConnectionUtils;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.ui.dialogs.CustomFragmentDialog;
import com.google.android.gms.maps.GoogleMap;

/**
 * This is the main activity that it is started every time and it is in charge of showing distinct UI stuff, menus, and also here we initialize the content view.
 */
public class MainActivity extends ServiceConnectActivity {

    public static final int REQUEST_CODE = 10001;
    private static final String ROUTE_ID_SAVE_KEY = "route_id_save_key";
    private static final String CUSTOM_TAG = "custom_tag2";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeToolbar();
        handleIntent(getIntent());
        handlePrefs(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setActionBarTitle();
    }

    private void handlePrefs(Bundle bundle) {
        if (bundle != null) {
            routeId = bundle.getLong(ROUTE_ID_SAVE_KEY, PreferenceUtils.DEFAULT_ROUTE_ID);
        }
    }

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

        int type = R.id.normal_view;
        if (this.getGoogleMap() != null) {
            type = this.getGoogleMap().getMapType();
        }

        if (item.getItemId() == R.id.normal_view) {
            type = GoogleMap.MAP_TYPE_NORMAL;
        } else if (item.getItemId() == R.id.satellite_view) {
            type = GoogleMap.MAP_TYPE_SATELLITE;
        } else if (item.getItemId() == R.id.route_detail_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        } else {
            return super.onOptionsItemSelected(item);
        }
        if (getGoogleMap() != null) {
            this.getGoogleMap().setMapType(type);
            item.setChecked(true);
        }
        return true;
    }


    @Override
    protected void onStop() {
        if (isRecordingServiceRunning()) {
            Bundle outState = new Bundle();
            outState.putLong(ROUTE_ID_SAVE_KEY, routeId);
            onSaveInstanceState(outState);
        }
        super.onStop();
    }

    protected GoogleMap getGoogleMap() {
        if (mMapFragment != null) {
            return mMapFragment.getGoogleMap();
        }
        return null;
    }

    public RecordingState getRecordingState() {
        if (getRecordFragment() != null) {
            return ((RecordControlsFragment) getRecordFragment()).getRecordState();
        }
        return RecordingState.NOT_STARTED;
    }


    public void setRecordingState(RecordingState state) {
        if (getRecordFragment() != null) {
            ((RecordControlsFragment) getRecordFragment()).setRecordingState(state);
        }
    }

    @Override
    protected void onDestroy() {
        resetPrefRouteAndFRecordState();
        super.onDestroy();
    }

    private void resetPrefRouteAndFRecordState() {
        if (!isRecordingServiceRunning()) {
            long recordingTrackId = PreferenceUtils.getLong(getApplicationContext(), R.string.route_id_key, -1);
            if (recordingTrackId != PreferenceUtils.DEFAULT_ROUTE_ID) {
                PreferenceUtils.setRouteId(getApplicationContext(), R.string.route_id_key, PreferenceUtils.DEFAULT_ROUTE_ID);
            }
            RecordingState recordingTrackPaused = PreferenceUtils.getRecordingState(getApplicationContext(),
                    R.string.recording_state_key, PreferenceUtils.RECORDING_STATE_NOT_STARTED_DEFAULT);
            if (recordingTrackPaused != RecordingState.NOT_STARTED) {
                PreferenceUtils.setRecordingState(getApplicationContext(), R.string.recording_state_key,
                        PreferenceUtils.RECORDING_STATE_NOT_STARTED_DEFAULT);
            }
        }
    }

    private boolean isRecordingServiceRunning() {
        return RecordingServiceConnectionUtils.isRecordingServiceRunning(getApplicationContext());
    }

    @Override
    public long getRouteId() {
        return routeId;
    }
}
