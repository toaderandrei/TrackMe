package com.ant.track.activities;

import android.location.Location;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;

import com.ant.track.IRecordingService;
import com.ant.track.fragments.RecordControlsFragment;
import com.ant.track.service.RecordingServiceConnection;
import com.ant.track.service.utils.RecordingServiceConnectionUtils;

/**
 * Created by Toader on 6/1/2015.
 * This activity is in charge of establishing the connection with the service - start/stopping the tracking.
 */
public abstract class ServiceConnectActivity extends BaseActivity {

    private static final String TAG = ServiceConnectActivity.class.getSimpleName();
    boolean isRecording = false;
    boolean isPaused = false;
    private RecordingServiceConnection mRecordingServiceConnection;
    private final View.OnClickListener stopListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            RecordingServiceConnectionUtils.stopRecording(mRecordingServiceConnection);
            isRecording = false;
            updateRecordState(isRecording());
        }
    };
    private boolean startNewRecording = false; // true to start a new recording
    private final Runnable bindToServiceCallback = new Runnable() {
        @Override
        public void run() {

            final IRecordingService service = mRecordingServiceConnection.getServiceIfBound();
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
                try {
                    service.startNewTracking();
                    startNewRecording = false;
                } catch (RemoteException ex) {
                    Log.e(TAG, ex.getMessage(), ex);
                }
            }

        }
    };
    private final View.OnClickListener recordListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (!isRecording() && !isPaused) {
                startRecording();
                isRecording = true;
                isPaused = false;
            } else if (!isRecording() && isPaused) {
                RecordingServiceConnectionUtils.startTracking(mRecordingServiceConnection);
                isRecording = true;
                isPaused = false;
            } else if (isRecording()) {
                RecordingServiceConnectionUtils.stopTracking(mRecordingServiceConnection);
                isRecording = false;
                isPaused = true;
            }
            updateRecordState(isRecording);
        }
    };

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
        RecordingServiceConnectionUtils.startConnection(this, mRecordingServiceConnection);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    protected void startRecording() {
        startNewRecording = true;
        mRecordingServiceConnection.startAndBind();

    /*
     * If the binding has happened, then invoke the callback to start a new
     * recording. If the binding hasn't happened, then invoking the callback
     * will have no effect. But when the binding occurs, the callback will get
     * invoked.
     */
        bindToServiceCallback.run();
    }

    protected IRecordingService getServiceIfBound() {
        return mRecordingServiceConnection.getServiceIfBound();
    }

    private void updateControlsOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateRecordControls();
                if (isRecording) {
                    try {
                        updateLocationFragment(getServiceIfBound().getLocation());
                    } catch (RemoteException ex) {
                        Log.e(TAG, ex.getMessage(), ex);
                    }
                }
            }
        });
    }

    protected void updateLocationFragment(Location loc) {
        if (mMapFragment != null && loc != null) {
            mMapFragment.getLocationListener().onLocationChanged(loc);
        }
    }

    protected void updateRecordControls() {
        updateRecordState(isRecording());
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

    @Override
    public void updateRecordState(boolean update) {
        ((RecordControlsFragment) this.getRecordControls()).update(update);
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public Location getLastLocation() {
        Location loc = null;
        if (getServiceIfBound() != null) {
            try {
                loc = getServiceIfBound().getLocation();
            } catch (RemoteException ex) {
                Log.e(TAG, ex.getMessage(), ex);
            }
        }
        return loc;
    }
}
