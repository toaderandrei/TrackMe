package com.ant.track.app.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;

import com.ant.track.app.R;
import com.ant.track.app.fragments.RecordControlsFragment;
import com.ant.track.app.fragments.ServiceHeadlessFragment;
import com.ant.track.app.helper.UiHelperUtils;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;

/**
 * This activity is in charge of establishing the connection with the service - start/stopping the tracking.
 */
public abstract class ServiceConnectActivity extends BaseActivity implements ServiceHeadlessFragment.Callback {

    private static final String TAG = ServiceConnectActivity.class.getSimpleName();
    public static final String GENERIC_ERROR_IN_STARTING_THE_LOCATION_SERVICE = "Generic Error in starting the location service!";
    private static final String RECORD_FRAGMENT_CONTROLS_TAG = "RECORD_FRAGMENT_CONTROLS_TAG";

    private SharedPreferences sharedPreferences;

    protected long routeId;
    protected long recordingRouteId;

    private static final String HEADLESS_TAG = "HEADLESS_TAG";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniRecordingAndHeadlessFragments();
    }

    protected void iniRecordingAndHeadlessFragments() {
        initRecordingFragment();
        initServiceHeadlessFragment();
    }

    protected void initRecordingFragment() {
        RecordControlsFragment recordControlsFragment = (RecordControlsFragment) getSupportFragmentManager().findFragmentByTag(RECORD_FRAGMENT_CONTROLS_TAG);
        if (recordControlsFragment == null) {
            recordControlsFragment = new RecordControlsFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fm_controls_container, recordControlsFragment, RECORD_FRAGMENT_CONTROLS_TAG);
            transaction.commit();
        }
    }

    protected void initServiceHeadlessFragment() {
        ServiceHeadlessFragment serviceFragment = (ServiceHeadlessFragment) getServiceFragment();
        if (serviceFragment == null) {
            serviceFragment = new ServiceHeadlessFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(serviceFragment, HEADLESS_TAG);
            transaction.commit();
        }
    }

    private Fragment getServiceFragment() {
        return getSupportFragmentManager().findFragmentByTag(HEADLESS_TAG);
    }

    protected Fragment getRecordFragment() {
        return getSupportFragmentManager().findFragmentByTag(RECORD_FRAGMENT_CONTROLS_TAG);
    }

    /*
  * Note that sharedPreferenceChangeListener cannot be an anonymous inner
  * class. Anonymous inner class will get garbage collected.
  */
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            // Note that key can be null
            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(ServiceConnectActivity.this, R.string.route_id_key))) {
                recordingRouteId = PreferenceUtils.getLong(ServiceConnectActivity.this, R.string.route_id_key);
            }
            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(ServiceConnectActivity.this, R.string.recording_state_key))) {
                final RecordingState recordingState = PreferenceUtils.getRecordingState(ServiceConnectActivity.this,
                        R.string.recording_state_key,
                        PreferenceUtils.RECORDING_STATE_NOT_STARTED_DEFAULT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateServiceState(recordingState);
                    }
                });
            }
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        sharedPreferences = this.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        sharedPreferenceChangeListener.onSharedPreferenceChanged(null, null);
    }

    @Override
    protected void onStop() {
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        }
        super.onStop();
    }

    @Override
    public void updateServiceState(RecordingState state) {
        Fragment serviceHeadlessFragment = getServiceFragment();
        if (serviceHeadlessFragment != null && serviceHeadlessFragment instanceof ServiceHeadlessFragment) {
            ((ServiceHeadlessFragment) serviceHeadlessFragment).updateServiceState(state);
        }
    }

    @Override
    public void onError(String message) {
        if (message == null) {
            message = GENERIC_ERROR_IN_STARTING_THE_LOCATION_SERVICE;
        }
        showErrToast(message);
    }

    @Override
    public void onUpdateUIControls(RecordingState recordingState) {
        //mMapFragment.clearPoints();
        updateUIControlsInternal(recordingState);
    }

    private void updateUIControlsInternal(RecordingState recordingState) {
        Fragment fragment = getRecordFragment();
        if (fragment != null && fragment instanceof RecordControlsFragment) {
            ((RecordControlsFragment) fragment).updateRecordState(recordingState);
        }
    }




    @Override
    public void updateRoute(long routeId) {
        if (mMapFragment != null) {
            mMapFragment.updateRoute(routeId);
        }
    }

    private void showErrToast(String errMessage) {
        UiHelperUtils.showErrToast(ServiceConnectActivity.this, errMessage);
    }
}
