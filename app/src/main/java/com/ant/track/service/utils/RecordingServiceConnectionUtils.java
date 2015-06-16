package com.ant.track.service.utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.ant.track.IRecordingService;
import com.ant.track.service.RecordingService;
import com.ant.track.service.RecordingServiceConnection;

import java.util.List;

/**
 * Created by Toader on 6/2/2015.
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
            if (RecordingService.class.getName().equals(serviceName)) {
                return true;
            }
        }
        return false;
    }

    public static void startConnection(Context context, RecordingServiceConnection mRecordingServiceConnection) {
        mRecordingServiceConnection.bindIfConnected();
    }


    /**
     * Resumes the recording track.
     *
     * @param mRecordingServiceConnection the track recording service
     */
    public static void startTracking(RecordingServiceConnection mRecordingServiceConnection) {
        try {
            IRecordingService service = mRecordingServiceConnection.getServiceIfBound();
            if (service != null) {
                service.startNewTracking();
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Unable to resume track.", e);
        }
    }

    /**
     * Stops the recording.
     *
     * @param mRecordingServiceConnection the recording service connection
     */
    public static void stopTracking(RecordingServiceConnection mRecordingServiceConnection) {
        IRecordingService mRecordingService = mRecordingServiceConnection.getServiceIfBound();
        if (mRecordingService != null) {
            try {
                mRecordingService.endCurrentTracking();
            } catch (Exception e) {
                Log.e(TAG, "Unable to stop recording.", e);
            }
        }
        //mRecordingServiceConnection.unbind();
    }

    /**
     * Stops the recording.
     *
     * @param mRecordingServiceConnection the recording service connection
     */
    public static void stopRecording(RecordingServiceConnection mRecordingServiceConnection) {
        IRecordingService mRecordingService = mRecordingServiceConnection.getServiceIfBound();
        if (mRecordingService != null) {
            try {
                mRecordingService.stopRecording();
            } catch (Exception e) {
                Log.e(TAG, "Unable to stop recording.", e);
            }
        }
        mRecordingServiceConnection.unbindAndStop();
    }
}
