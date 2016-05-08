package com.ant.track.activities;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import com.ant.track.R;
import com.ant.track.fragments.LocationFragment;
import com.ant.track.fragments.RecordControlsFragment;
import com.ant.track.helper.UiHelperUtils;
import com.ant.track.service.RecordingServiceConnection;
import com.ant.track.service.utils.RecordingServiceConnectionUtils;

/**
 * This activity is in charge of establishing the connection with the service - start/stopping the tracking.
 */
public abstract class ServiceConnectActivity extends BaseActivity {

    private static final String TAG = ServiceConnectActivity.class.getSimpleName();
    public static final String GENERIC_ERROR_IN_STARTING_THE_LOCATION_SERVICE = "Generic Error in starting the location service!";
    boolean isRecording = false;
    boolean isPaused = false;
    private RecordingServiceConnection mRecordingServiceConnection;
    private boolean startNewRecording = false; // true to start a new recording

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRecordingServiceConnection = new RecordingServiceConnection(this, bindToServiceCallback);

    }

    @Override
    protected void initRecordingFragment() {
        super.initRecordingFragment();
        updateRecordingFragment();
    }

    private void updateRecordingFragment() {
        if (mRecordFragment != null) {
            mRecordFragment.updateRecordListeners(recordListener, stopListener);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Update the recording service connection
        RecordingServiceConnectionUtils.startConnection(mRecordingServiceConnection);
    }

    private void updateControlsOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRecordControls();
            }
        });
    }

    protected void updateRecordControls() {
        updateRecordState(isRecording());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LocationFragment.CONNECTION_RESOLUTION_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Fragment fragment = getFragmentById(R.id.container);
                        if (isMapLocationFragment(fragment)) {
                            ((LocationFragment) fragment).initGpsTracker(false);
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        showErrToast("Gps not enabled:");
                        break;
                    default:
                        showErrToast("Gps not enabled:");
                        break;
                }
                break;
        }
    }

    private boolean isMapLocationFragment(Fragment fragment) {
        return fragment != null && fragment instanceof LocationFragment;
    }

    @Override
    protected void onStop() {
        super.onStop();
        mRecordingServiceConnection.unbind();
    }

    @Override
    protected void onDestroy() {
        if (!isRecording && isPaused) {
            mRecordingServiceConnection.unbindAndStop();
        }
        super.onDestroy();
    }


    private final View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RecordingServiceConnectionUtils.stopTracking(mRecordingServiceConnection);
            isRecording = false;
            updateRecordState(isRecording());
        }
    };

    private final RecordingServiceConnection.Callback bindToServiceCallback = new RecordingServiceConnection.Callback() {

        @Override
        public void onConnected() {

            final IBinder service = mRecordingServiceConnection.getServiceIfBound();
            if (service == null) {
                Log.d(TAG, "service not available to start a new recording");
                return;
            }
            if (!startNewRecording) {
                isRecording = true;
                updateControlsOnUiThread();
                return;
            }

            if (startNewRecording) {
                RecordingServiceConnectionUtils.startTracking(mRecordingServiceConnection);
                startNewRecording = false;
            }
        }

        @Override
        public void onLocationUpdate(Location location) {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onError(String errrMessage) {
            if (errrMessage == null) {
                errrMessage = GENERIC_ERROR_IN_STARTING_THE_LOCATION_SERVICE;
            }
            showErrToast(errrMessage);
            stopTrackingService();
        }
    };

    private final View.OnClickListener recordListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!isRecording() && !isPaused) {
                startTrackingService();
            } else if (!isRecording() && isPaused) {
                resumeTracking();
            } else if (isRecording()) {
                stopTracking();
            }
            updateRecordState(isRecording);
        }
    };

    private void startTrackingService() {
        RecordingServiceConnectionUtils.startTrackingService(mRecordingServiceConnection);
        isRecording = true;
        isPaused = false;
        startNewRecording = true;
    }

    private void resumeTracking() {
        RecordingServiceConnectionUtils.resumeTracking(mRecordingServiceConnection);
        isRecording = true;
        isPaused = false;
    }

    private void stopTracking() {
        RecordingServiceConnectionUtils.stopTracking(mRecordingServiceConnection);
        isRecording = false;
        isPaused = true;
    }

    private void stopTrackingService() {
        RecordingServiceConnectionUtils.stopTrackingService(mRecordingServiceConnection);
        isRecording = false;
        isPaused = true;
    }

    @Override
    public void updateRecordState(boolean update) {
        ((RecordControlsFragment) this.getRecordControls()).update(update);
    }

    private Fragment getFragmentById(int id) {
        return getSupportFragmentManager().findFragmentById(id);
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    private void showErrToast(String errMessage) {
        UiHelperUtils.showErrToast(ServiceConnectActivity.this, errMessage);
    }
}
