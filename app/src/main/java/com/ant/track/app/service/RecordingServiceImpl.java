package com.ant.track.app.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.ant.track.app.R;
import com.ant.track.app.activities.MainActivity;
import com.ant.track.app.application.GPSLiveTrackerApplication;
import com.ant.track.app.helper.IntentUtils;
import com.ant.track.app.helper.LocationUtils;
import com.ant.track.app.helper.SystemUtils;
import com.ant.track.app.location.AbsoluteLocationListenerRestrictions;
import com.ant.track.app.location.GPSLiveTrackerLocationManager;
import com.ant.track.app.location.LocationListenerRestrictions;
import com.ant.track.app.provider.IDataProvider;
import com.ant.track.app.service.utils.UnitConversions;
import com.ant.track.lib.PreferenceUtils;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.db.content.TrackMeDatabaseUtils;
import com.ant.track.lib.db.content.TrackMeDatabaseUtilsImpl;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RouteStats;
import com.google.android.gms.location.LocationListener;

import java.lang.ref.WeakReference;


/**
 * Tracking Service
 */
public class RecordingServiceImpl extends Service {
    private long routeId;
    private static final long ONE_MINUTE = (long) (UnitConversions.MIN_TO_S * UnitConversions.S_TO_MS);
    // 1 second in milliseconds
    private static final long ONE_SECOND = (long) UnitConversions.S_TO_MS;
    private static final String TAG = RecordingServiceImpl.class.getSimpleName();
    public static final String NOT_ALLOWED_TO_ACCESS_THE_LOCATION_SERVICES = "Not allowed to access the location services";
    private Handler handlerService;
    private HandlerThread handlerThread;
    private LocationListenerRestrictions locationListenerPolicy;
    private int minRecordingInterval = 20;
    private GPSLiveTrackerLocationManager mLiveTrackingLocationManager;
    private int recordingGpsAccuracy = LocationUtils.RECORDING_GPS_ACCURACY_DEFAULT;
    private boolean isRecording = false;
    private PowerManager.WakeLock wakeLock;
    private SharedPreferences sharedPrefs;
    private Location mLastLocation;
    private WeakReference<Context> contextWeakRef;
    private long currentRecordingInterval;
    private Handler handler;
    private Messenger serviceMessenger;
    private RouteStats routeStats;
    private TrackMeDatabaseUtils trackMeDatabaseUtils;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if (mLiveTrackingLocationManager == null || !mLiveTrackingLocationManager.isAllowed()) {
                sendErrorLocationUpdate(NOT_ALLOWED_TO_ACCESS_THE_LOCATION_SERVICES, RecordingServiceConstants.MSG_NOT_ALLOWED);
                return;
            }
            runAsync(new Runnable() {
                @Override
                public void run() {
                    onLocationChangedAsync(location);
                }
            });
        }
    };
    private final Runnable registerLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRecording()) {
                startRequestingLocationUpdates();
            }
            handler.postDelayed(this, ONE_MINUTE);
        }
    };


    private SharedPreferences.OnSharedPreferenceChangeListener shareListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            //todo check the strings
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initAsyncThread();
        serviceMessenger = new Messenger(handlerService);
        mLiveTrackingLocationManager = new GPSLiveTrackerLocationManager(this);
        contextWeakRef = new WeakReference<Context>(this);
        locationListenerPolicy = new AbsoluteLocationListenerRestrictions(minRecordingInterval * ONE_SECOND);
        handler.post(registerLocationRunnable);
        trackMeDatabaseUtils = TrackMeDatabaseUtilsImpl.getInstance();
        routeId = PreferenceUtils.DEFAULT_ROUTE_ID;
        getSharedPrefs().registerOnSharedPreferenceChangeListener(shareListener);
        shareListener.onSharedPreferenceChanged(getSharedPrefs(), null);
        showNotification();
    }

    private SharedPreferences getSharedPrefs() {
        if (sharedPrefs == null) {
            sharedPrefs = PreferenceUtils.getSharedPrefs(this);
        }
        return sharedPrefs;
    }

    protected void startRequestingLocationUpdates() {
        if (mLiveTrackingLocationManager == null) {
            Log.e(TAG, "locationManager is null.");
            return;
        }
        try {
            long interval = locationListenerPolicy.getDesiredPollingInterval();
            mLiveTrackingLocationManager.requestLocationUpdates(interval, locationListenerPolicy.getMinDistance(), locationListener);
            currentRecordingInterval = interval;
        } catch (RuntimeException e) {
            Log.e(TAG, "Could not register location listener.", e);
        }
    }

    private void initAsyncThread() {
        handlerThread = new HandlerThread("ServiceThread");
        handlerThread.start();
        handlerService = new IncomingHandler(handlerThread.getLooper());
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    private void runAsync(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        handlerService.post(runnable);
    }


    protected void onLocationChangedAsync(Location location) {
        if (!isRecording()) {
            Log.w(TAG, "Ignore onLocationChangedAsync. Not recording.");
            return;
        }
        if (!LocationUtils.isValidLocation(location)) {
            Log.w(TAG, "Ignore onLocationChangedAsync. location is invalid.");
            return;
        }

        if (!location.hasAccuracy() || location.getAccuracy() >= recordingGpsAccuracy) {
            Log.d(TAG, "Ignore onLocationChangedAsync. Poor accuracy.");
            return;
        }
        Location lastValidPoint = getLastLocation();
        long idleTime = 0;
        if (lastValidPoint != null && location.getTime() > lastValidPoint.getTime()) {
            idleTime = location.getTime() - lastValidPoint.getTime();
        }
        locationListenerPolicy.updateIdleTime(idleTime);
        if (currentRecordingInterval != locationListenerPolicy.getDesiredPollingInterval()) {
            startRequestingLocationUpdates();
        }
        if (lastValidPoint == null) {
            mLastLocation = location;
        }
        updateLocation(location, lastValidPoint);
    }

    protected void updateLocation(Location location, Location lastValidLocation) {
        if (location == null) {
            Log.w(TAG, "Ignore insertLocation. location is null.");
            return;
        }
        // Do not insert if inserted already
        if (lastValidLocation != null && lastValidLocation.getTime() == location.getTime()) {
            Log.w(TAG, "Ignore the updating of the location. location time same as last valid location time.");
            return;
        }
        sendLocationUpdate(location);
    }

    private void sendLocationUpdate(Location location) {
        if (serviceMessenger != null) {
            Message message = Message.obtain(null, RecordingServiceConstants.MSG_UPDATE_LOCATION, 0, 0);
            message.obj = location;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException remex) {
                Log.e(TAG, "Exception in sending the message" + remex.getMessage());
            }
        }
    }

    private void sendErrorLocationUpdate(String errMessage, int messageId) {
        if (serviceMessenger != null) {
            Message message = Message.obtain(null, messageId, 0, 0);
            message.obj = errMessage;
            try {
                serviceMessenger.send(message);
            } catch (RemoteException remex) {
                Log.e(TAG, "Exception in sending the message" + remex.getMessage());
            }
        }
    }


    public boolean isRecording() {
        if (!canAccess()) {
            return false;
        }
        return isRecording;
    }

    public void startNewTracking() {
        if (isRecording()) {
            Log.d(TAG, "Ignore startNewTracking. Already recording.");
            return;
        }
        startRecording();
    }


    private void startRecording() {
        mLastLocation = null;
        isRecording = true;
        startGPSTracking();
    }

    /**
     * Starts gps.
     */
    private void startGPSTracking() {
        wakeLock = SystemUtils.acquireWakeLock(this, wakeLock);
        startRouteTracking();
        startRequestingLocationUpdates();
        showNotification();
    }

    private void startRouteTracking() {
        long now = System.currentTimeMillis();
        Route route = new Route();
        Uri uriRouteInsert = trackMeDatabaseUtils.insertRouteTrack(route);
        long id = Long.parseLong(uriRouteInsert.getLastPathSegment());
        PreferenceUtils.setRouteId(this, R.string.route_id_key, id);
        routeStats = new RouteStats(now);

    }

    private void endTracking(boolean stopped) {
        if (!canAccess()) {
            return;
        }
        if (!isRecording()) {
            Log.d(TAG, "Ignore endCurrentTrack. Not recording.");
            return;
        }
        stopRecordingService(stopped);
    }

    private void stopRecordingService(boolean stopped) {
        mLastLocation = null;
        isRecording = false;
        stopGpsTracking(stopped);
    }

    /**
     * Stops gps.
     *
     * @param stop true to stop self
     */
    private void stopGpsTracking(boolean stop) {
        unregisterLocationListener();
        stopNotifications();
        releaseWakeLock();
        if (stop) {
            stopSelf();
        }
    }

    /**
     * Shows the notification.
     */
    private void showNotification() {
        Intent intent = IntentUtils.newIntentWithAction(IntentUtils.MAIN_ACTIVITY_ACTION).putExtra(Constants.EXTRA_RECORDING_ID, isRecording);
        PendingIntent pendingIntent = TaskStackBuilder.create(this).addParentStack(MainActivity.class).addNextIntent(intent).getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        startForegroundService(pendingIntent, R.string.tracking_record_notification);
        sendMessageToListeners(RecordingServiceConstants.MSG_SHOW_NOTIFICATIONS);
        // Not recording
    }

    private void stopNotifications() {
        stopForegroundService();
        sendMessageToListeners(RecordingServiceConstants.MSG_STOP_NOTIFICATIONS);
    }


    /**
     * Starts the service as a foreground service.
     *
     * @param pendingIntent the notification pending intent
     * @param messageId     the notification message id
     */
    protected void startForegroundService(PendingIntent pendingIntent, int messageId) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this).setContentIntent(
                pendingIntent).setContentText(getString(messageId))
                .setContentTitle(getString(R.string.app_name)).setOngoing(true)
                .setSmallIcon(R.drawable.ic_stat_notify_recording).setWhen(System.currentTimeMillis());
        startForeground(1, builder.build());
    }

    /**
     * Stops the service as a foreground service.
     */
    protected void stopForegroundService() {
        stopForeground(true);
    }

    /**
     * Unregisters the location manager.
     */
    private void unregisterLocationListener() {
        if (mLiveTrackingLocationManager == null) {
            Log.e(TAG, "locationManager is null.");
            return;
        }
        mLiveTrackingLocationManager.removeLocationUpdates(locationListener);
    }

    protected void deInitAsyncThread() {

        handlerThread = null;
        handlerService = null;
    }

    /**
     * Releases the wake lock.
     */
    private void releaseWakeLock() {
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }
    }

    /**
     * Service Binder class that implements the aidl interface
     */

    private class IncomingHandler extends Handler {
        IncomingHandler(Looper lopper) {
            super(lopper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case RecordingServiceConstants.MSG_START_TRACKING:
                    startNewTracking();
                    break;
                case RecordingServiceConstants.MSG_END_TRACKING:
                    endTracking(true);
                    break;
                case RecordingServiceConstants.MSG_STOP_SERVICE:
                    stopRecordingService(true);
                    break;
            }
        }
    }

    private boolean canAccess() {
        if (serviceMessenger == null) {
            throw new IllegalStateException("The track recording service has been detached!");
        }
        //TODO check for the processes - which process accesses it.
        return true;
    }

    private IDataProvider getDataProvider() {
        return getApp().getDataProvider();
    }

    private GPSLiveTrackerApplication getApp() {
        return (GPSLiveTrackerApplication) GPSLiveTrackerApplication.getInstance();
    }


    /**
     * sends messages to the listeners
     *
     * @param messageId the id to identify the type of message.
     */
    private void sendMessageToListeners(int messageId) {

        switch (messageId) {
            case RecordingServiceConstants.MSG_SHOW_NOTIFICATIONS:
                //todo notify ui
                break;
            case RecordingServiceConstants.MSG_STOP_NOTIFICATIONS:
                //todo notify ui
                break;
        }
    }

    protected Location getLastLocation() {
        return mLastLocation;
    }

    @Override
    public void onDestroy() {
        stopNotifications();
        handler.removeCallbacks(registerLocationRunnable);
        deInitAsyncThread();
        unregisterLocationListener();
        mLiveTrackingLocationManager.close();
        mLiveTrackingLocationManager = null;
        //binder.detachFromService();
        //binder = null;
        // This should be the next to last operation
        releaseWakeLock();
        TrackMeDatabaseUtilsImpl.reset();
        trackMeDatabaseUtils = null;
        super.onDestroy();
    }

}
