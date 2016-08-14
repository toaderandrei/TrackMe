package com.ant.track.app.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.ant.track.app.BuildConfig;
import com.ant.track.app.R;
import com.ant.track.app.service.utils.RecordingServiceConnectionUtils;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;

import java.lang.ref.WeakReference;

/**
 * Class that deals with starting and stopping the location
 * service.
 */
public class RecordingServiceConnection {

    private static final String TAG = RecordingServiceConnection.class.getSimpleName();
    private final Callback callback;
    private WeakReference<Context> contextRef;

    /**
     * Constructor.
     *
     * @param context  the context
     * @param callback the callback to invoke when the service binding changes
     */
    public RecordingServiceConnection(Context context, Callback callback) {
        this.contextRef = new WeakReference<>(context);
        this.callback = callback;
    }

    private final IBinder.DeathRecipient deathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.d(TAG, "Service died.");
            setRecordingService(null);
        }
    };

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder binder) {
            Log.i(TAG, "Connected to the service.");
            try {
                binder.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to bind a death recipient.", e);
            }
            setRecordingService(IRecordingService.Stub.asInterface(binder));
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.i(TAG, "Disconnected from the service.");
            setRecordingService(null);
        }
    };

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
        contextRef.get().stopService(new Intent(contextRef.get(), RecordingServiceImpl.class));
    }

    /**
     * Unbinds the service (but leave it running).
     */
    public void unbind() {
        try {
            contextRef.get().unbindService(serviceConnection);
        } catch (IllegalArgumentException e) {
            // Means not bound to the service. OK to ignore.
        }
        setRecordingService(null);
    }

    /**
     * Gets the track recording service if bound. Returns null otherwise
     */
    public IRecordingService getServiceIfBound() {
        if (recordingService != null && !recordingService.asBinder().isBinderAlive()) {
            setRecordingService(null);
            return null;
        }
        return recordingService;
    }

    /**
     * Binds the service if it is started.
     *
     * @param startIfNeeded start the service if needed
     */
    private void bindService(boolean startIfNeeded) {
        if (recordingService != null) {
            // Service is already started and bound.
            return;
        }

        if (!startIfNeeded && !RecordingServiceConnectionUtils.isRecordingServiceRunning(contextRef.get())) {
            Log.d(TAG, "Service is not started. Not binding it.");
            return;
        }

        if (startIfNeeded) {
            Log.i(TAG, "Starting the service.");
            contextRef.get().startService(new Intent(contextRef.get(), RecordingServiceImpl.class));
        }

        Log.i(TAG, "Binding the service.");
        int flags = BuildConfig.DEBUG ? Context.BIND_DEBUG_UNBIND : 0;
        contextRef.get().bindService(new Intent(contextRef.get(), RecordingServiceImpl.class), serviceConnection, flags);
    }



    private IRecordingService recordingService;

    /**
     * Sets the mRecordingService.
     *
     * @param service the service
     */
    private void setRecordingService(IRecordingService service) {

        recordingService = service;
        if (callback != null) {
            if (service != null) {
                callback.onConnected();
            } else {
                callback.onDisconnected();
            }
        }
    }

    public interface Callback {

        /**
         * callback for when the service has been connected.
         */
        void onConnected();

        /**
         * callback for when the service has been disconnected.
         **/
        void onDisconnected();
    }
}
