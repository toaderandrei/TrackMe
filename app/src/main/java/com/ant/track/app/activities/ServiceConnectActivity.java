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

    private long routeId;
    private long recordingRouteId;
    private RecordControlsFragment recordControlsFragment;

    private static final String HEADLESS_TAG = "HEADLESS_TAG";
    private ServiceHeadlessFragment serviceFragment;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = this.getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        sharedPreferenceChangeListener.onSharedPreferenceChanged(null, null);
    }

    protected void initRecordingAndServiceFragment() {
        initRecordingFragment();
        initServiceHeadlessFragment();
    }

    protected void initRecordingFragment() {
        recordControlsFragment = (RecordControlsFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_record_controls);
        if (recordControlsFragment == null) {
            recordControlsFragment = new RecordControlsFragment();
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(recordControlsFragment, RECORD_FRAGMENT_CONTROLS_TAG);
            transaction.commit();
        }
    }

    protected void initServiceHeadlessFragment() {
        serviceFragment = (ServiceHeadlessFragment) getServiceFragment();
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

    /*
  * Note that sharedPreferenceChangeListener cannot be an anonymous inner
  * class. Anonymous inner class will get garbage collected.
  */
    private final SharedPreferences.OnSharedPreferenceChangeListener
            sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            // Note that key can be null
            if (TextUtils.equals(key, PreferenceUtils.getKey(ServiceConnectActivity.this, R.string.route_id_key))) {
                recordingRouteId = PreferenceUtils.getLong(ServiceConnectActivity.this, R.string.route_id_key);
            }
            if (TextUtils.equals(key, PreferenceUtils.getKey(ServiceConnectActivity.this, R.string.recording_state_key))) {
                recordingState = PreferenceUtils.getRecordingState(ServiceConnectActivity.this,
                        R.string.recording_state_key,
                        PreferenceUtils.RECORDING_STATE_PAUSED_DEFAULT);
            }
            if (key != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        boolean isRecording = routeId == recordingRouteId;
                        if (isRecording) {
                            recordingState = RecordingState.STARTED;
                        }
                        updateServiceState(recordingState);
                    }
                });
            }
        }
    };

    @Override
    protected void onStop() {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
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
        recordControlsFragment.updateRecordState(recordingState);
    }


    @Override
    public void updateRoute(long routeId) {
        this.routeId = routeId;
    }

    private void showErrToast(String errMessage) {
        UiHelperUtils.showErrToast(ServiceConnectActivity.this, errMessage);
    }
}
