package com.ant.track.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ant.track.app.R;
import com.ant.track.app.activities.MainActivity;
import com.ant.track.app.activities.RouteDetailsActivity;
import com.ant.track.app.application.TrackMeApplication;
import com.ant.track.app.service.IRecordingService;
import com.ant.track.app.service.RecordingServiceConnection;
import com.ant.track.app.service.utils.RecordingServiceConnectionUtils;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.ui.dialogs.CustomFragmentDialog;

/**
 * Fragment with no UI, used mainly for starting/stopping the service.
 * In order to deal with the rotation issues, the fragment has a setRetain instance.
 */
public class ServiceHeadlessFragment extends Fragment {

    private static final String CUSTOM_TAG = "ServiceHeadlessCustomTag";
    private RecordingServiceConnection mRecordingServiceConnection;

    private static final String TAG = ServiceHeadlessFragment.class.getCanonicalName();
    private Callback callback;
    private RecordingState recordingState = RecordingState.NOT_STARTED;
    private boolean startNewRecording = false;
    private long routeId;

    private SharedPreferences sharedPreferences;

    /**
     * Note that sharedPreferenceChangeListener cannot be an anonymous inner
     * class. Anonymous inner class will get garbage collected.
     */
    private final SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            // Note that key can be null
            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(getActivity(), R.string.route_id_key))) {
                routeId = PreferenceUtils.getLong(getActivity(), R.string.route_id_key);
            }
            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(getActivity(), R.string.recording_state_key))) {
                recordingState = PreferenceUtils.getRecordingState(getActivity(),
                        R.string.recording_state_key,
                        PreferenceUtils.RECORDING_STATE_NOT_STARTED_DEFAULT);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onUpdateUIControls(recordingState);
                    }
                });
            }
        }
    };


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

        sharedPreferences = getActivity().getSharedPreferences(Constants.SETTINGS_NAME, Context.MODE_PRIVATE);

        sharedPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        sharedPreferenceChangeListener.onSharedPreferenceChanged(null, null);
    }

    @Override
    public void onStop() {
        if (sharedPreferences != null) {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();

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

            IRecordingService service = mRecordingServiceConnection.getServiceIfBound();
            if (service == null) {
                Log.d(TAG, "service not available to start gps or a new recording");
                return;
            }
            if (startNewRecording) {
                try {
                    Log.d(TAG, "service is starting.");
                    startNewRecording = false;
                    routeId = service.startNewRoute();
                    callback.updateRoute(routeId);
                    recordingState = RecordingState.STARTED;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUIControls();
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
        public void onDisconnected() {
            startNewRecording = true;
            recordingState = RecordingState.NOT_STARTED;
            callback.onUpdateUIControls(recordingState);
        }
    };

    private void runOnUiThread(final Runnable runnable) {
        getActivity().runOnUiThread(runnable);
    }

    private void startTrackingService() {
        startNewRecording = true;
        RecordingServiceConnectionUtils.startTrackingService(mRecordingServiceConnection);
    }

    private void resumeTracking() {
        RecordingServiceConnectionUtils.resumeTracking(mRecordingServiceConnection);
        recordingState = RecordingState.RESUMED;
        updateUIControls();
    }

    private void pauseTracking() {
        RecordingServiceConnectionUtils.pauseTracking(mRecordingServiceConnection);
        recordingState = RecordingState.PAUSED;
        updateUIControls();
    }

    private void stopTracking() {
        showCustomDialog();
    }

    private void showCustomDialog() {
        Bundle bundle = new Bundle();
        long recordingId = PreferenceUtils.getLong(getActivity(), R.string.route_id_key, PreferenceUtils.DEFAULT_ROUTE_ID);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        bundle.putLong(Constants.EXTRA_ROUTE_ID_KEY, recordingId);
        bundle.putBoolean(Constants.EXTRA_VIEW_ROUTE_DETAILS, true);
        CustomFragmentDialog customFragmentDialog = CustomFragmentDialog.newInstance(getString(R.string.view_route_details),
                getString(R.string.view_route_details_message),
                getString(R.string.ok),
                getString(R.string.cancel),
                dialogCallback,
                bundle);
        customFragmentDialog.show(fragmentManager, CUSTOM_TAG);
    }

    CustomFragmentDialog.Callback dialogCallback = new CustomFragmentDialog.Callback() {
        @Override
        public void onPositiveButtonClicked(Bundle bundle) {
            stopRouteTrackingInternal();

            //start activity
            callback.onUpdateUIControls(RecordingState.NOT_STARTED);
            PreferenceUtils.setRecordingState(TrackMeApplication.getInstance().getApplicationContext(), R.string.recording_state_key, RecordingState.NOT_STARTED);
            Intent intent = new Intent(getActivity(), RouteDetailsActivity.class);
            intent.putExtras(bundle);
            startActivityForResult(intent, MainActivity.REQUEST_CODE);
        }

        @Override
        public void onNegativeButtonClicked(Bundle bundle) {
            stopRouteTrackingInternal();
        }
    };

    private void stopRouteTrackingInternal() {
        recordingState = RecordingState.STOPPED;
        updateUIControls();

        RecordingServiceConnectionUtils.stopTrackingService(getActivity(), mRecordingServiceConnection);
    }

    private void updateUIControls() {
        callback.onUpdateUIControls(recordingState);
    }

    public void updateServiceState(RecordingState recordingState) {
        if (recordingState == RecordingState.PAUSED) {
            pauseTracking();
        } else if (recordingState == RecordingState.STARTING) {
            startTrackingService();
        } else if (recordingState == RecordingState.RESUMED) {
            resumeTracking();
        } else if (recordingState == RecordingState.STOPPED) {
            stopTracking();
        }
    }

    public long getRouteId() {
        return routeId;
    }

    public RecordingState getRecordingState() {
        return recordingState;
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
    }
}
