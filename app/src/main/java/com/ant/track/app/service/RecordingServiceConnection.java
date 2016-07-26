package com.ant.track.app.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import com.ant.track.app.BuildConfig;
import com.ant.track.app.service.utils.RecordingServiceConnectionUtils;

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
     * Messenger for sending messages to the service.
     */
    private Messenger mServiceMessenger = null;
    /**
     * Messenger for receiving messages from the service.
     */
    private Messenger mClientMessenger = null;
    private IncomingHandler incomingHandler;

    /**
     * Constructor.
     *
     * @param context  the context
     * @param callback the callback to invoke when the service binding changes
     */
    public RecordingServiceConnection(Context context, Callback callback) {
        this.contextRef = new WeakReference<>(context);
        this.callback = callback;
        incomingHandler = new IncomingHandler(Looper.getMainLooper());
        mClientMessenger = new Messenger(incomingHandler);
    }

    private class IncomingHandler extends Handler {

        public IncomingHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RecordingServiceConstants.MSG_UPDATE_LOCATION:
                    if (callback != null && msg.obj != null) {
                        callback.onLocationUpdate((Location) msg.obj);
                    }
                    break;
                case RecordingServiceConstants.MSG_NOT_ALLOWED:
                    if (callback != null && msg.obj != null) {
                        callback.onError((String) msg.obj);
                    }
                    break;
            }
        }
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
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.i(TAG, "Connected to the service.");
            try {
                service.linkToDeath(deathRecipient, 0);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to bind a death recipient.", e);
            }
            setRecordingService(service);
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
    public IBinder getServiceIfBound() {
        if (mServiceMessenger != null && !mServiceMessenger.getBinder().isBinderAlive()) {
            setRecordingService(null);
            return null;
        }
        if (mServiceMessenger != null) {
            return mServiceMessenger.getBinder();
        }
        return null;
    }

    /**
     * Binds the service if it is started.
     *
     * @param startIfNeeded start the service if needed
     */
    private void bindService(boolean startIfNeeded) {
        if (mServiceMessenger != null && mServiceMessenger.getBinder() != null) {
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

    /**
     * Sets the mRecordingService.
     *
     * @param binder the binder
     */
    private void setRecordingService(IBinder binder) {

        try {
            if (binder != null) {
                attachListenerToService(binder);
            } else {
                mServiceMessenger = null;
                callback.onDisconnected();
            }
        } catch (RemoteException remex) {
            Log.e(TAG, "Exception in sending the message to the service");
            if (callback != null) {
                callback.onError(remex.getMessage());
            }
        }
    }

    private void attachListenerToService(IBinder binder) throws RemoteException {
        mServiceMessenger = new Messenger(binder);
        Message message = Message.obtain(null, RecordingServiceConstants.MSG_SERVICE_CALL, 0, 0);
        message.replyTo = mClientMessenger;
        mServiceMessenger.send(message);
        if (callback != null) {
            callback.onConnected();
        }
    }

    public void startTracking() throws RemoteException {
        Message message = Message.obtain(null, RecordingServiceConstants.MSG_START_TRACKING, 0, 0);
        mServiceMessenger.send(message);
    }

    public void resumeTracking() throws RemoteException {
        if (mServiceMessenger != null) {
            Message message = Message.obtain(null, RecordingServiceConstants.MSG_RESUME_TRACKING, 0, 0);
            mServiceMessenger.send(message);
        }
    }

    public void pauseTracking() throws RemoteException {
        if (mServiceMessenger != null) {
            Message message = Message.obtain(null, RecordingServiceConstants.MSG_PAUSE_TRACKING, 0, 0);
            mServiceMessenger.send(message);
        }
    }

    public void stopTracking() throws RemoteException {
        if (mServiceMessenger != null) {
            Message message = Message.obtain(null, RecordingServiceConstants.MSG_STOP_TRACKING, 0, 0);
            mServiceMessenger.send(message);
        }
    }

    public interface Callback {

        /**
         * callback for when a new location came from the service
         *
         * @param location location to be updated.
         */
        void onLocationUpdate(Location location);

        /**
         * callback for when the service has been connected.
         */
        void onConnected();

        /**
         * callback for when the service has been disconnected.
         */
        void onDisconnected();

        /**
         * Notification about an error that occurred.
         */
        void onError(String message);
    }
}
