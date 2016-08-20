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
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;

/**
 * This activity is in charge of establishing the connection with the service - start/stopping the tracking.
 */
public abstract class ServiceConnectActivity extends BaseActivity implements ServiceHeadlessFragment.Callback {

    private static final String TAG = ServiceConnectActivity.class.getSimpleName();
    private static final String RECORD_FRAGMENT_CONTROLS_TAG = "RECORD_FRAGMENT_CONTROLS_TAG";

    protected long routeId;
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

    @Override
    public void updateServiceState(RecordingState state) {
        Fragment serviceHeadlessFragment = getServiceFragment();
        if (serviceHeadlessFragment != null && serviceHeadlessFragment instanceof ServiceHeadlessFragment) {
            ((ServiceHeadlessFragment) serviceHeadlessFragment).updateServiceState(state);
        }
    }

    @Override
    public void onUpdateUIControls(RecordingState recordingState) {
        if (recordingState == RecordingState.NOT_STARTED || recordingState == RecordingState.STOPPED) {
            if (mMapFragment != null) {
                mMapFragment.clearPoints();
            }
        }
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

    public RecordingState getRecordingState() {
        ServiceHeadlessFragment serviceHeadlessFragment = (ServiceHeadlessFragment) getServiceFragment();
        if (serviceHeadlessFragment != null) {
            return serviceHeadlessFragment.getRecordingState();
        }
        return RecordingState.NOT_STARTED;
    }
    
    @Override
    public void onUpdateLockControls(boolean lock) {
        Fragment fragment = getRecordFragment();
        if (fragment != null && fragment instanceof RecordControlsFragment) {
            ((RecordControlsFragment) fragment).updateLockControls(lock);
        }
    }
    
    @Override
    public long getRouteId() {
        ServiceHeadlessFragment serviceHeadlessFragment = (ServiceHeadlessFragment) getServiceFragment();
        if (serviceHeadlessFragment != null) {
            return serviceHeadlessFragment.getRouteId();
        }
        return -1L;
    }
}
