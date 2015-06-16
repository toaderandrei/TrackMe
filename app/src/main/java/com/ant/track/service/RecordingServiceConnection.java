package com.ant.track.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ant.track.BuildConfig;
import com.ant.track.IRecordingService;
import com.ant.track.service.utils.RecordingServiceConnectionUtils;

/**
 * Created by Toader on 6/2/2015.
 */
public class RecordingServiceConnection {

    private static final String TAG = RecordingServiceConnection.class.getSimpleName();
    private final Context context;
    private final Runnable callback;
    private IRecordingService mRecordingService;

    private final IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "Service died.");
            setRecordingService(null);
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "Connected to the service.");
            try {
                service.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to bind a death recipient.", e);
            }
            setRecordingService(IRecordingService.Stub.asInterface(service));
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG, "Disconnected from the service.");
            setRecordingService(null);
        }
    };

    /**
     * Constructor.
     *
     * @param context  the context
     * @param callback the callback to invoke when the service binding changes
     */
    public RecordingServiceConnection(Context context, Runnable callback) {
        this.context = context;
        this.callback = callback;
    }

    /**
     * Starts and binds the service.
     */
    public void startAndBind() {
        bindService(true);
    }

    /**
     * Binds the service if it is started.
     */
    public void bindIfConnected() {
        bindService(false);
    }

    /**
     * Unbinds and stops the service.
     */
    public void unbindAndStop() {
        unbind();
        context.stopService(new Intent(context, RecordingService.class));
    }

    /**
     * Unbinds the service (but leave it running).
     */
    public void unbind() {
        try {
            context.unbindService(serviceConnection);
        } catch (IllegalArgumentException e) {
            // Means not bound to the service. OK to ignore.
        }
        setRecordingService(null);
    }

    /**
     * Gets the track recording service if bound. Returns null otherwise
     */
    public IRecordingService getServiceIfBound() {
        if (mRecordingService != null && !mRecordingService.asBinder().isBinderAlive()) {
            setRecordingService(null);
            return null;
        }
        return mRecordingService;
    }

    /**
     * Binds the service if it is started.
     *
     * @param startIfNeeded start the service if needed
     */
    private void bindService(boolean startIfNeeded) {
        if (mRecordingService != null) {
            // Service is already started and bound.
            return;
        }

        if (!startIfNeeded && !RecordingServiceConnectionUtils.isRecordingServiceRunning(context)) {
            Log.d(TAG, "Service is not started. Not binding it.");
            return;
        }

        if (startIfNeeded) {
            Log.i(TAG, "Starting the service.");
            context.startService(new Intent(context, RecordingService.class));
        }

        Log.i(TAG, "Binding the service.");
        int flags = BuildConfig.DEBUG ? Context.BIND_DEBUG_UNBIND : 0;
        context.bindService(new Intent(context, RecordingService.class), serviceConnection, flags);
    }

    /**
     * Sets the mRecordingService.
     *
     * @param value the value
     * TODO - modify so that it uses aidl callbacks.
     */
    private void setRecordingService(IRecordingService value) {
        mRecordingService = value;
        if (callback != null) {
            callback.run();
        }
    }
}
