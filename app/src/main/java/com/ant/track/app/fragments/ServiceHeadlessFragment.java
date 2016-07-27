package com.ant.track.app.fragments;

import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ant.track.app.R;
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
    private boolean startNewRecording = true;
    private long routeId;

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


            if (!startNewRecording) {
                return;
            }

            IBinder service = mRecordingServiceConnection.getServiceIfBound();
            if (service == null) {
                Log.d(TAG, "service not available to start gps or a new recording");
                return;
            }
            if (startNewRecording) {
                try {
                    mRecordingServiceConnection.startTracking();
                    recordingState = RecordingState.STARTED;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onUpdateUIControls(recordingState);
                        }
                    });
                } catch (RemoteException e) {
                    Toast.makeText(
                            getActivity(), R.string.recording_record_error, Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Unable to start a new recording.", e);
                }
            }

        }

        @Override
        public void onRouteUpdate(long id) {
            routeId = id;
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


    private void runOnUiThread(final Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }


    private void startTrackingService() {
        RecordingServiceConnectionUtils.startTrackingService(mRecordingServiceConnection);
    }

    private void resumeTracking() {
        RecordingServiceConnectionUtils.resumeTracking(mRecordingServiceConnection);
        //todo should be done with ca callback.
        recordingState = RecordingState.RESUMED;
    }

    private void pauseTracking() {
        RecordingServiceConnectionUtils.stopTracking(mRecordingServiceConnection);
        //todo should be done with a callback
        recordingState = RecordingState.PAUSED;
    }

    private void stopTracking() {
        RecordingServiceConnectionUtils.stopTrackingService(mRecordingServiceConnection);
        //todo should be done with a callback.
        recordingState = RecordingState.STOPPED;
    }

    public void updateServiceState(RecordingState recordingState) {
        if (recordingState == RecordingState.PAUSED) {
            pauseTracking();
        } else if (recordingState == RecordingState.STARTING) {
            startTrackingService();
        } else if (recordingState == RecordingState.RESUMED) {
            resumeTracking();
        } else {
            stopTracking();
        }
    }

    public interface Callback {

        /**
         * update the Ui regarding the current routeid.
         *
         * @param routeId the id of the running route.
         */
        void updateRoute(long routeId);

        /**
         * updates the UI controls.
         */
        void onUpdateUIControls(RecordingState state);

        void onError(String message);
    }
}
