package com.ant.track.app.service.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.ant.track.app.service.RecordingServiceConnection;
import com.ant.track.app.service.RecordingServiceImpl;

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
    }


    /**
     * Resumes the recording track.
     *
     * @param mRecordingServiceConnection the track recording service
     */
    public static void startTracking(RecordingServiceConnection mRecordingServiceConnection) {
        try {
            if (mRecordingServiceConnection != null) {
                mRecordingServiceConnection.startTracking();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to start the tracking.", e);
        }
    }

    public static void pauseTracking(RecordingServiceConnection mRecordingServiceConnection) {
        try {
            if (mRecordingServiceConnection != null) {
                mRecordingServiceConnection.pauseTracking();
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
    public static void stopTrackingService(RecordingServiceConnection mRecordingServiceConnection) {
        if (mRecordingServiceConnection != null) {
            try {
                mRecordingServiceConnection.stopTracking();
                mRecordingServiceConnection.unbindAndStop();
            } catch (Exception e) {
                Log.e(TAG, "Unable to stop tracking.", e);
            }
        }
    }

    public static void resumeTracking(RecordingServiceConnection mRecordingServiceConnection) {
        if (mRecordingServiceConnection != null) {
            try {
                mRecordingServiceConnection.resumeTracking();
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
}
