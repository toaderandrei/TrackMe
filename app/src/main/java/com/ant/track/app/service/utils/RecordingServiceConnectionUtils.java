package com.ant.track.app.service.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.ant.track.app.R;
import com.ant.track.app.application.TrackMeApplication;
import com.ant.track.app.service.IRecordingService;
import com.ant.track.app.service.RecordingServiceConnection;
import com.ant.track.app.service.RecordingServiceImpl;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.ui.dialogs.CustomFragmentDialog;

import java.util.List;

/**
 *
 */
public class RecordingServiceConnectionUtils {
    private static final String TAG = RecordingServiceConnectionUtils.class.getSimpleName();

    private RecordingServiceConnectionUtils() {
    }

    /**
     * Returns true if the recording service is running.
     *
     * @param context the current context
     */
    public static boolean isRecordingServiceRunning(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (ActivityManager.RunningServiceInfo serviceInfo : services) {
            ComponentName componentName = serviceInfo.service;
            String serviceName = componentName.getClassName();
            if (RecordingServiceImpl.class.getName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static void startConnection(RecordingServiceConnection mRecordingServiceConnection) {
        mRecordingServiceConnection.bindIfConnected();
        if (!isRecordingServiceRunning(TrackMeApplication.getInstance())) {
            resetRecordingState(TrackMeApplication.getInstance());
        }
    }


    /**
     * Resumes the recording track.
     *
     * @param mRecordingServiceConnection the track recording service
     */
    public static void startTracking(RecordingServiceConnection mRecordingServiceConnection) {
        try {
            if (mRecordingServiceConnection != null) {
                IRecordingService service = mRecordingServiceConnection.getServiceIfBound();
                if (service != null) {
                    service.startNewRoute();
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to start the tracking.", e);
        }
    }

    public static void pauseTracking(RecordingServiceConnection mRecordingServiceConnection) {
        try {
            if (mRecordingServiceConnection != null) {
                IRecordingService service = mRecordingServiceConnection.getServiceIfBound();

                if (service != null) {
                    service.pauseCurrentRoute();
                }
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to start the tracking.", e);
        }
    }

    /**
     * Stops the recording.
     *
     * @param mRecordingServiceConnection the recording service connection
     */
    public static void stopTrackingService(Context context, RecordingServiceConnection mRecordingServiceConnection) {
        if (mRecordingServiceConnection != null) {
            try {
                IRecordingService service = mRecordingServiceConnection.getServiceIfBound();
                if (service != null) {

                    service.stopCurrentRoute();
                }
            } catch (Exception e) {
                Log.e(TAG, "Unable to stop tracking.", e);
            }
            resetRecordingState(context);
            mRecordingServiceConnection.unbindAndStop();
        }
    }

    public static void resumeTracking(RecordingServiceConnection mRecordingServiceConnection) {
        if (mRecordingServiceConnection != null) {
            try {
                IRecordingService service = mRecordingServiceConnection.getServiceIfBound();
                if (service != null) {
                    service.resumeRecordingService();
                }
            } catch (Exception e) {
                Log.e(TAG, "Unable to resume tracking.", e);
            }
        }
    }

    public static void startTrackingService(RecordingServiceConnection mRecordingServiceConnection) {
        if (mRecordingServiceConnection != null) {
            try {
                mRecordingServiceConnection.startAndBind();
            } catch (Exception e) {
                Log.e(TAG, "Unable to start the recording.", e);
            }
        }
    }

    private static void showDetails(long routeid) {

    }

    /**
     * in cases when there is a crash and we want a full reset.
     */
    private static void resetRecordingState(Context context) {
        long recordingTrackId = PreferenceUtils.getLong(context, R.string.route_id_key, -1);
        if (recordingTrackId != PreferenceUtils.DEFAULT_ROUTE_ID) {
            PreferenceUtils.setRouteId(context, R.string.route_id_key, PreferenceUtils.DEFAULT_ROUTE_ID);
        }
        RecordingState recordingTrackPaused = PreferenceUtils.getRecordingState(context,
                R.string.recording_state_key, PreferenceUtils.RECORDING_STATE_NOT_STARTED_DEFAULT);
        if (recordingTrackPaused != RecordingState.NOT_STARTED) {
            PreferenceUtils.setRecordingState(context, R.string.recording_state_key,
                    PreferenceUtils.RECORDING_STATE_NOT_STARTED_DEFAULT);
        }
    }
}
