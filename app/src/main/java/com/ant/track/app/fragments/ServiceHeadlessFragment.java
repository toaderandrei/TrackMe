package com.ant.track.app.fragments;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ant.track.app.application.TrackMeApplication;
import com.ant.track.app.service.RecordingServiceConnection;
import com.ant.track.app.service.utils.RecordingServiceConnectionUtils;
import com.ant.track.lib.service.RecordingState;

/**
 * Fragment with no UI, used mainly for starting/stopping the service.
 * In order to deal with the rotation issues, the fragment has a setRetain instance.
 */
public class ServiceHeadlessFragment extends Fragment {

    private RecordingServiceConnection mRecordingServiceConnection;

    private static final String TAG = ServiceHeadlessFragment.class.getCanonicalName();
    private Callback callback;
    private RecordingState recordingState = RecordingState.NOT_STARTED;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callback = (Callback) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mRecordingServiceConnection = new RecordingServiceConnection(TrackMeApplication.getInstance(), bindToServiceCallback);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        RecordingServiceConnectionUtils.startConnection(mRecordingServiceConnection);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (recordingState == RecordingState.STOPPED) {
            mRecordingServiceConnection.unbindAndStop();
        }
        super.onDestroy();
    }

    private final RecordingServiceConnection.Callback bindToServiceCallback = new RecordingServiceConnection.Callback() {
        @Override
        public void onConnected() {
            //updateServiceState(recordingState);
        }

        @Override
        public void onLocationUpdate(Location location) {

        }

        @Override
        public void onDisconnected() {

        }

        @Override
        public void onError(String errMessage) {
            callback.onError(errMessage);
            stopTracking();
        }
    };


    private void startTrackingService() {
        RecordingServiceConnectionUtils.startTrackingService(mRecordingServiceConnection);
        recordingState = RecordingState.STARTED;
    }

    private void resumeTracking() {
        RecordingServiceConnectionUtils.resumeTracking(mRecordingServiceConnection);
        recordingState = RecordingState.RESUMED;
    }

    private void pauseTracking() {
        RecordingServiceConnectionUtils.stopTracking(mRecordingServiceConnection);
        recordingState = RecordingState.PAUSED;
    }

    private void stopTracking() {
        RecordingServiceConnectionUtils.stopTrackingService(mRecordingServiceConnection);
        recordingState = RecordingState.STOPPED;
    }

    public void updateServiceState(RecordingState recordingState) {
        if (recordingState == RecordingState.PAUSED) {
            pauseTracking();
        } else if (recordingState == RecordingState.STARTED) {
            startTrackingService();
        } else if (recordingState == RecordingState.RESUMED) {
            resumeTracking();
        } else {
            stopTracking();
        }
    }

    private void updateUIRecordState(RecordingState update) {
        callback.onUpdateUIControls(update);
    }

    public interface Callback {
        /**
         * updates the UI controls.
         */
        void onUpdateUIControls(RecordingState state);

        void onError(String message);
    }
}
