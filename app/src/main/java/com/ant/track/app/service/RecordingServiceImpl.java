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
import android.text.TextUtils;
import android.util.Log;

import com.ant.track.app.R;
import com.ant.track.app.activities.MainActivity;
import com.ant.track.app.application.GPSLiveTrackerApplication;
import com.ant.track.app.helper.IntentUtils;
import com.ant.track.app.helper.SystemUtils;
import com.ant.track.app.location.GPSLiveTrackerLocationManager;
import com.ant.track.app.location.LocationListenerRestrictions;
import com.ant.track.app.location.LocationListenerRestrictionsImpl;
import com.ant.track.app.location.RecordingInterval;
import com.ant.track.app.provider.IDataProvider;
import com.ant.track.app.service.utils.UnitConversions;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.db.content.TrackMeDatabaseUtils;
import com.ant.track.lib.db.content.TrackMeDatabaseUtilsImpl;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.stats.RouteStatsManager;
import com.ant.track.lib.utils.LocationUtils;
import com.google.android.gms.location.LocationListener;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Tracking Service
 */
public class RecordingServiceImpl extends Service {

    private long routeId;
    private static double PAUSE_LATITUDE = 100.0d;

    private static final long ONE_MINUTE = (long) (UnitConversions.MIN_TO_S * UnitConversions.S_TO_MS);
    // 1 second in milliseconds
    private static final long ONE_SECOND = (long) UnitConversions.S_TO_MS;
    private static final String TAG = RecordingServiceImpl.class.getSimpleName();
    public static final String NOT_ALLOWED_TO_ACCESS_THE_LOCATION_SERVICES = "Not allowed to access the location services";
    private Handler handlerService;
    private HandlerThread handlerThread;
    private LocationListenerRestrictions locationListenerPolicy;
    private int minRecordingIntervalTime = 20;
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
    private RouteStatsManager routeStatsManager;
    private RecordingState recordingState;
    private int minRecordingDistance;
    private int maxRecordingDistance;
    private AtomicBoolean isIdle = new AtomicBoolean(false);
    //location listener policy
    //Battery life
    private static final long DEFAULT_BATTERY_MIN_INTERVAL = 30 * ONE_SECOND;
    private static final long DEFAULT_BATTERY_MAX_INTERVAL = 5 * ONE_MINUTE;
    private static final int DEFAULT_BATTERY_MIN_DISTANCE = 5;

    //high accuracy
    private static final long DEFAULT_HIGH_ACCURACY_MIN_INTERVAL = ONE_SECOND;
    private static final long DEFAULT_HIGH_ACCURACY_MAX_INTERVAL = ONE_MINUTE;
    private static final int DEFAULT_ACCURACY_MIN_DISTANCE = 5;

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

            if (!TextUtils.isEmpty(key)) {
                if (TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.route_id_key))) {
                    routeId = PreferenceUtils.getLong(contextWeakRef.get(), R.string.route_id_key, -1);
                }

                //gps accuracy
                if (TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.recording_gps_accuracy_key))) {
                    recordingGpsAccuracy = PreferenceUtils.getInt(contextWeakRef.get(),
                            R.string.recording_gps_accuracy_key,
                            PreferenceUtils.RECORDING_GPS_ACCURACY_DEFAUL);
                }

                //distance interval
                if (TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.recording_distance_interval_key))) {
                    minRecordingDistance = PreferenceUtils.getInt(contextWeakRef.get(),
                            R.string.recording_distance_interval_key,
                            PreferenceUtils.RECORDING_DISTANCE_DEFAULT);
                }

                //distance interval
                if (TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.max_recording_distance_key))) {
                    maxRecordingDistance = PreferenceUtils.getInt(contextWeakRef.get(),
                            R.string.recording_distance_interval_key,
                            PreferenceUtils.DEFAULT_MAX_RECORDING_DISTANCE);
                }

                //location listener policy
                if (TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.recording_location_threshold_key))) {
                    int recordingThreshold = PreferenceUtils.getInt(contextWeakRef.get(),
                            R.string.recording_location_threshold_key,
                            PreferenceUtils.RECORDING_GPS_ACCURACY_DEFAUL);

                    switch (recordingThreshold) {
                        case RecordingInterval.BATTERY_LIFE:
                            locationListenerPolicy = new LocationListenerRestrictionsImpl(DEFAULT_BATTERY_MIN_INTERVAL,
                                    DEFAULT_BATTERY_MAX_INTERVAL, DEFAULT_BATTERY_MIN_DISTANCE);
                            break;
                        case RecordingInterval.ACCURACY:
                            locationListenerPolicy = new LocationListenerRestrictionsImpl(DEFAULT_HIGH_ACCURACY_MIN_INTERVAL,
                                    DEFAULT_HIGH_ACCURACY_MAX_INTERVAL, DEFAULT_ACCURACY_MIN_DISTANCE);
                            break;
                        default:
                            locationListenerPolicy = new LocationListenerRestrictionsImpl(recordingThreshold * ONE_SECOND);
                            break;
                    }
                }
                //
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        initAsyncThread();
        serviceMessenger = new Messenger(handlerService);
        mLiveTrackingLocationManager = new GPSLiveTrackerLocationManager(this);
        contextWeakRef = new WeakReference<Context>(this);
        locationListenerPolicy = new LocationListenerRestrictionsImpl(minRecordingIntervalTime * ONE_SECOND);
        handler.post(registerLocationRunnable);
        trackMeDatabaseUtils = TrackMeDatabaseUtilsImpl.getInstance();
        routeId = PreferenceUtils.DEFAULT_ROUTE_ID;
        getSharedPrefs().registerOnSharedPreferenceChangeListener(shareListener);
        shareListener.onSharedPreferenceChanged(getSharedPrefs(), null);

        //this is useful in case a service restart occurs - which happens
        Route route = TrackMeDatabaseUtilsImpl.getInstance().getRouteById(routeId);
        if (route != null) {
            restartRoute();
        } else {
            if (isRecording()) {
                //cannot be paused - just started
                updateRecordingState(PreferenceUtils.DEFAULT_ROUTE_ID, RecordingState.STARTED);
            }
            showNotification();
        }
    }


    private void updateRecordingState(long _routeId, RecordingState state) {
        routeId = _routeId;
        PreferenceUtils.setRouteId(this, R.string.route_id_key, routeId);
        recordingState = state;
        PreferenceUtils.setString(this, R.string.recording_state_key, recordingState.getState());
    }

    private void restartRoute() {
        //todo restart the route from the last point - last id inserted.
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
        if (!isRecording() || isPaused()) {
            Log.w(TAG, "Ignore onLocationChangedAsync. Not recording or we are in paused state.");
            return;
        }

        Route route = trackMeDatabaseUtils.getRouteById(routeId);
        if (route == null) {
            Log.e(TAG, "invalid route.");
        }

        if (location == null) {
            Log.w(TAG, "Ignore insertLocation. location is null.");
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
        Location lastValidTrackLocation = getLastValidTrackLocation();
        long idleTime = 0;
        if (lastValidTrackLocation != null && location.getTime() > lastValidTrackLocation.getTime()) {
            idleTime = location.getTime() - lastValidTrackLocation.getTime();
        }

        locationListenerPolicy.updateIdleTime(idleTime);
        if (currentRecordingInterval != locationListenerPolicy.getDesiredPollingInterval()) {
            startRequestingLocationUpdates();
        }

        //we check if either the last valid location is invalid or if it is null, meaning there was
        // none so far.
        if (!LocationUtils.isValidLocation(lastValidTrackLocation)) {
            insertLocation(route, location, null);
            mLastLocation = location;
            return;
        }

        double distance = location.distanceTo(getLastValidRouteTrack(routeId));

        //this can happen - in case of a tunnel longer than max distance
        if (distance > maxRecordingDistance) {
            //todo we need to find a way that can show on the map and on the track that we basically had a long location or something in between.
            //todo tunnel mode can happen?
            Location pauseLocation = new Location("GPS");
            pauseLocation.setLatitude(PAUSE_LATITUDE);
            //todo think about a better time, maybe a mix between last valid and current location?
            pauseLocation.setTime(location.getTime());
            insertLocation(route, pauseLocation, lastValidTrackLocation);
            insertLocation(route, location, lastValidTrackLocation);
            isIdle.set(false);
        } else if (distance >= minRecordingDistance) {
            insertLocation(route, getLastLocation(), lastValidTrackLocation);
            insertLocation(route, location, lastValidTrackLocation);
            isIdle.set(false);
        } else if (!isIdle.get() && location.hasSpeed() && location.getSpeed() <= Constants.MAX_SPEED_NO_MOVEMENT) {
            //looks that it is idle
            //todo think if it makes sense to insert the location when it is inside the recording distance(min and also if the speed it is less the min movement.
            isIdle.set(true);
        } else if (isIdle.get() && location.hasSpeed() && location.getSpeed() > Constants.MAX_SPEED_NO_MOVEMENT) {
            isIdle.set(false);
            insertLocation(route, getLastLocation(), lastValidTrackLocation);
            insertLocation(route, location, lastValidTrackLocation);
        }

        mLastLocation = location;
    }

    protected void insertLocation(Route route, Location location, Location lastValidLocation) {
        // Do not insert if inserted already
        if (lastValidLocation != null && lastValidLocation.getTime() == location.getTime()) {
            Log.w(TAG, "Ignore the updating of the location. location time same as last valid location time.");
            return;
        }

        //insert it to the stats.
        try {
            Uri uri = trackMeDatabaseUtils.insertRoutePoint(route.getRouteId(), location);
            long id = Long.parseLong(uri.getLastPathSegment());
            routeStatsManager.addLocationToStats(location, minRecordingDistance);
            updateRecordingRoute(route, id, LocationUtils.isValidLocation(location));
        } catch (Exception exc) {
            sendErrorLocationUpdate(new ErrorLocation(exc, location));
        }
        sendLocationUpdate(location);
    }

    private void updateRecordingRoute(Route route, long lastInsertedId, boolean isValidLocation) {
        if (lastInsertedId > 0) {
            //success of insertion
            if (route.getStartPointId() <= 0) {
                route.setStartPointId(lastInsertedId);
            }

            route.setStopPointId(lastInsertedId);
        }

        if (isValidLocation) {
            route.setNumberOfPoints(route.getNumberOfPoints() + 1);
        }
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

    private void sendErrorLocationUpdate(ErrorLocation errorLocation) {
        if (serviceMessenger != null) {
            Message message = Message.obtain(null, RecordingServiceConstants.MSG_UPDATE_LOCATION, 0, 0);
            message.obj = errorLocation;
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
        return recordingState == RecordingState.RESUMED || recordingState == RecordingState.STARTED;
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
        startRouteTracking();
        showNotification();
    }

    /**
     * Starts gps.
     */
    private void startGPSTracking() {
        wakeLock = SystemUtils.acquireWakeLock(this, wakeLock);
        startRequestingLocationUpdates();
    }

    private void startRouteTracking() {
        long now = System.currentTimeMillis();
        routeStatsManager = new RouteStatsManager(now);
        Route route = new Route();
        Uri uriRouteInsert = trackMeDatabaseUtils.insertRouteTrack(route);
        long insertedRouteId = Long.parseLong(uriRouteInsert.getLastPathSegment());
        PreferenceUtils.setRouteId(this, R.string.route_id_key, insertedRouteId);
        route.setRouteId(insertedRouteId);
        routeId = insertedRouteId;
        updateRecordingState(routeId, RecordingState.STARTED);
    }

    private void resumeTracking() {
        Log.d(TAG, "resume the route track");
        if (isResumed()) {
            Log.d(TAG, " already resumed");
            return;
        }
        updateRecordingState(routeId, RecordingState.RESUMED);

        Route pausedRoute = trackMeDatabaseUtils.getRouteById(routeId);
        if (pausedRoute != null) {
            //todo resuming...
        }
        startGPSTracking();
    }

    private void pauseTracking() {

        Log.d(TAG, "pausing the route track");

        updateRecordingState(routeId, RecordingState.PAUSED);
        Route route = trackMeDatabaseUtils.getRouteById(routeId);

        if (route != null) {
            insertLocation(route, mLastLocation, getLastValidRouteTrack(route.getRouteId()));
        }

        stopRecordingService(false);
    }

    private void stopTracking(boolean stopped) {
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
                case RecordingServiceConstants.MSG_STOP_TRACKING:
                    stopTracking(true);
                    break;
                case RecordingServiceConstants.MSG_PAUSE_TRACKING:
                    pauseTracking();
                    break;

                case RecordingServiceConstants.MSG_RESUME_TRACKING:
                    resumeTracking();
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


    protected Location getLastValidTrackLocation() {
        return trackMeDatabaseUtils.getLastValidLocation(routeId);
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

    private Location getLastValidRouteTrack(long routeId) {
        return trackMeDatabaseUtils.getLastValidRouteTrack(routeId);
    }

    private boolean isPaused() {
        return recordingState == RecordingState.PAUSED;
    }

    private boolean isResumed() {
        return recordingState == RecordingState.RESUMED;
    }

    private IDataProvider getDataProvider() {
        return getApp().getDataProvider();
    }

    private GPSLiveTrackerApplication getApp() {
        return (GPSLiveTrackerApplication) GPSLiveTrackerApplication.getInstance();
    }

}
