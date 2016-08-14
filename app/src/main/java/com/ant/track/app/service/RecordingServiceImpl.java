package com.ant.track.app.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.util.Log;

import com.ant.track.app.R;
import com.ant.track.app.activities.MainActivity;
import com.ant.track.app.application.TrackMeApplication;
import com.ant.track.app.helper.IntentUtils;
import com.ant.track.app.helper.SystemUtils;
import com.ant.track.app.location.GPSLiveTrackerLocationManager;
import com.ant.track.app.location.LocationListenerRestrictions;
import com.ant.track.app.location.LocationListenerRestrictionsImpl;
import com.ant.track.app.location.RecordingInterval;
import com.ant.track.app.provider.IDataProvider;
import com.ant.track.app.service.utils.UnitConversions;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.content.factory.LocationIterator;
import com.ant.track.lib.content.factory.TrackMeDatabaseUtilsImpl;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.model.RoutePoint;
import com.ant.track.lib.model.RouteTrackCreator;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.lib.stats.RouteStats;
import com.ant.track.lib.stats.RouteStatsManager;
import com.ant.track.lib.utils.LocationUtils;
import com.google.android.gms.location.LocationListener;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Tracking Service
 */
public class RecordingServiceImpl extends Service {

    /**
     * data used for testing. When enabled, it will create
     * mock locations using the start location.
     */
    //used for testing only
    private boolean testAllowed = false;
    private Handler mockHandler;
    private Location mockLocation = null;
    // end for testing
    /**
     * this variable is specifically for creating mock locations.
     * Enabled will mess up with the gps and send fake locations.
     */
    private static final long DEFAULT_ROUTE_POINT_ID = -1L;
    private long routeId;
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
    private PowerManager.WakeLock wakeLock;
    private SharedPreferences sharedPrefs;
    private Location mLastLocation;
    private WeakReference<Context> contextWeakRef;
    private long currentRecordingInterval;
    private Handler handler;
    private boolean firstInsert = true;

    private ServiceBinder serviceBinder = new ServiceBinder(this);
    private RouteStatsManager routeStatsManager;
    private RecordingState recordingState = RecordingState.NOT_STARTED;
    private int minRecordingDistance = 1;
    private int maxRecordingDistance = PreferenceUtils.DEFAULT_MAX_RECORDING_DISTANCE;
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

    //private TrackMeDatabaseUtils trackMeDatabaseUtils;

    private LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            if (mLiveTrackingLocationManager == null || !mLiveTrackingLocationManager.isAllowed()) {
                //sendErrorLocationUpdate(NOT_ALLOWED_TO_ACCESS_THE_LOCATION_SERVICES, RecordingServiceConstants.MSG_NOT_ALLOWED);
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

            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.route_id_key))) {
                routeId = PreferenceUtils.getLong(contextWeakRef.get(), R.string.route_id_key, -1);
            }

            //gps accuracy
            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.recording_gps_accuracy_key))) {
                recordingGpsAccuracy = PreferenceUtils.getInt(contextWeakRef.get(),
                        R.string.recording_gps_accuracy_key,
                        PreferenceUtils.RECORDING_GPS_ACCURACY_DEFAULT);
            }

            //distance interval
            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.recording_distance_interval_key))) {
                minRecordingDistance = PreferenceUtils.getInt(contextWeakRef.get(),
                        R.string.recording_distance_interval_key,
                        PreferenceUtils.RECORDING_DISTANCE_DEFAULT);
            }

            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.recording_state_key))) {
                recordingState = PreferenceUtils.getRecordingState(contextWeakRef.get(),
                        R.string.recording_state_key,
                        PreferenceUtils.RECORDING_STATE_NOT_STARTED_DEFAULT);
            }

            //distance interval
            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.max_recording_distance_key))) {
                maxRecordingDistance = PreferenceUtils.getInt(contextWeakRef.get(),
                        R.string.recording_distance_interval_key,
                        PreferenceUtils.DEFAULT_MAX_RECORDING_DISTANCE);
            }

            //location listener policy
            if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(contextWeakRef.get(), R.string.recording_location_threshold_key))) {
                int recordingThreshold = PreferenceUtils.getInt(contextWeakRef.get(),
                        R.string.recording_location_threshold_key,
                        PreferenceUtils.RECORDING_GPS_ACCURACY_DEFAULT);

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
    };

    @Override
    public void onCreate() {
        super.onCreate();
        //test
        if (testAllowed) {
            mockHandler = new Handler(Looper.getMainLooper());
        }
        initAsyncThread();
        mLiveTrackingLocationManager = new GPSLiveTrackerLocationManager(this);
        contextWeakRef = new WeakReference<Context>(this);
        locationListenerPolicy = new LocationListenerRestrictionsImpl(minRecordingIntervalTime * ONE_SECOND);
        handler.post(registerLocationRunnable);
        //trackMeDatabaseUtils = TrackMeDatabaseUtilsImpl.getInstance();
        routeId = PreferenceUtils.DEFAULT_ROUTE_ID;
        getSharedPrefs().registerOnSharedPreferenceChangeListener(shareListener);
        shareListener.onSharedPreferenceChanged(getSharedPrefs(), null);

        //this is useful in case a service restart occurs - which happens
        Route route = TrackMeDatabaseUtilsImpl.getInstance().getRouteById(routeId);
        if (route != null) {
            restartRoute(route);
        } else {
            if (isRecording()) {
                //cannot be paused - just started
                updateRecordingState(PreferenceUtils.DEFAULT_ROUTE_ID, RecordingState.STARTED);
            }
            showNotification();
        }
    }

    private void initMockTimer() {
        mockHandler.postDelayed(mockRunnable, 3000);
    }

    @NonNull
    private Runnable mockRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "reload location mock timer");
            Location lastValidLoc = getLastValidLocationForRoute();
            if (mockLocation != null) {
                lastValidLoc = mockLocation;
                lastValidLoc.setAccuracy(3.0f);
                lastValidLoc.setLatitude(mockLocation.getLatitude() + 0.00011);
                lastValidLoc.setLongitude(mockLocation.getLongitude() + 0.000011);

            } else if (lastValidLoc != null) {
                lastValidLoc.setLatitude(lastValidLoc.getLatitude() + 0.00011);
                lastValidLoc.setLongitude(lastValidLoc.getLongitude() + 0.0000011);
                lastValidLoc.setAccuracy(3.0f);
                lastValidLoc.setTime(System.currentTimeMillis());

            }

            if (lastValidLoc != null) {
                Log.d(TAG, "notification about location");
                Log.d(TAG, "routestats here: " + routeStatsManager);
                onLocationChangedAsync(lastValidLoc);
            }

            mockLocation = lastValidLoc;
            mockHandler.postDelayed(this, 3000);
        }
    };


    private void updateRecordingState(Long _routeId, RecordingState state) {
        routeId = _routeId;
        PreferenceUtils.setRouteId(this, R.string.route_id_key, routeId);
        recordingState = state;
        PreferenceUtils.setRecordingState(this, R.string.recording_state_key, state);
    }

    private void restartRoute(Route route) {
        //first get the stats from route.
        RouteStats routeStats = route.getRouteStats();
        RouteStatsManager routeStatsManager = new RouteStatsManager(routeStats.getStartTime());

        //try to add all the locations using a location iterator and the start time
        LocationIterator locationIterator = null;
        try {
            locationIterator = TrackMeDatabaseUtilsImpl.getInstance().getRoutePointsIterator(route.getRouteId(), -1L);

            while (locationIterator.hasNext()) {
                Location location = locationIterator.next();
                routeStatsManager.addLocationToStats(location, minRecordingDistance);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "RuntimeException", e);
        } finally {
            if (locationIterator != null) {
                locationIterator.close();
            }
        }
        startRecording();
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
        handlerService = new Handler(handlerThread.getLooper());
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return serviceBinder;
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

        Route route = TrackMeDatabaseUtilsImpl.getInstance().getRouteById(routeId);
        if (route == null) {
            Log.e(TAG, "invalid route.");
            return;
        }

        if (location == null) {
            Log.w(TAG, "Ignore insertLocation. location is null.");
            return;
        }

        if (!LocationUtils.isValidLocation(location)) {
            Log.w(TAG, "Ignore onLocationChangedAsync. location is invalid.");
            return;
        }

        if (!location.hasAccuracy() || location.getAccuracy() > recordingGpsAccuracy) {
            Log.d(TAG, "Ignore onLocationChangedAsync. Poor accuracy.");
            mockLocation = location;
            return;
        }
        Location lastValidTrackLocation = getLastValidLocationForRoute();
        long idleTime = 0;
        if (lastValidTrackLocation != null && location.getTime() > lastValidTrackLocation.getTime()) {
            idleTime = location.getTime() - lastValidTrackLocation.getTime();
        }

        locationListenerPolicy.updateIdleTime(idleTime);
        if (currentRecordingInterval != locationListenerPolicy.getDesiredPollingInterval()) {
            startRequestingLocationUpdates();
        }

        if (firstInsert) {
            insertLocation(route, location, null);
            mLastLocation = location;
            firstInsert = false;
            return;

        }

        //we check if either the last valid location is invalid or if it is null, meaning there was
        // none so far.
        if (!LocationUtils.isValidLocation(lastValidTrackLocation)) {
            insertLocation(route, location, null);
            mLastLocation = location;
            return;
        }

        double distance = location.distanceTo(lastValidTrackLocation);

        //this can happen - in case of a tunnel longer than max distance
        if (distance > maxRecordingDistance) {
            //todo we need to find a way that can show on the map and on the track that we basically had a long location or something in between.
            //todo tunnel mode can happen?
            Location pauseLocation = new Location(LocationManager.GPS_PROVIDER);
            pauseLocation.setLatitude(Constants.PAUSE_LATITUDE);
            //todo think about a better time, maybe a mix between last valid and current location?
            pauseLocation.setTime(mLastLocation.getTime());
            insertLocation(route, pauseLocation, lastValidTrackLocation);
            insertLocation(route, location, lastValidTrackLocation);
            isIdle.set(false);
        } else if (distance >= minRecordingDistance) {
            insertLocation(route, getLastLocation(), lastValidTrackLocation);
            insertLocation(route, location, lastValidTrackLocation);
            isIdle.set(false);
        } else if (!isIdle.get() && location.hasSpeed() && location.getSpeed() <= Constants.MAX_SPEED_NO_MOVEMENT) {
            //looks that it is idle
            //todo think if it makes sense to insert the location when it is inside the recording distance(min) and also if the speed it is less the min movement.
            //insertLocation(route, getLastLocation(), lastValidTrackLocation);
            //insertLocation(route, location, lastValidTrackLocation);
            isIdle.set(true);
        } else if (isIdle.get() && location.hasSpeed() && location.getSpeed() > Constants.MAX_SPEED_NO_MOVEMENT) {
            isIdle.set(false);
            insertLocation(route, getLastLocation(), lastValidTrackLocation);
            insertLocation(route, location, lastValidTrackLocation);
        }

        mLastLocation = location;
    }

    protected boolean insertLocation(Route route, Location location, Location lastValidLocation) {
        // Do not insert if inserted already
        //todo - fix this weird nullpointer.
        if (lastValidLocation != null && location != null && lastValidLocation.getTime() == location.getTime()) {
            Log.w(TAG, "Ignore the updating of the location. location time same as last valid location time.");
            return false;
        }

        //insert it to the stats.
        try {
            Uri uri = TrackMeDatabaseUtilsImpl.getInstance().insertRoutePoint(route.getRouteId(), location);
            long id = Long.parseLong(uri.getLastPathSegment());
            routeStatsManager.addLocationToStats(location, minRecordingDistance);

            updateRecordingRoute(route, id, LocationUtils.isValidLocation(location));

            sendRouteBroadcast(R.string.route_update_broadcast_action, route.getRouteId());

        } catch (Exception exc) {
            return false;
        }
        return true;

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

        //we update the route
        routeStatsManager.updateTime(System.currentTimeMillis());
        route.setRouteStats(routeStatsManager.getCurrentRouteStats());
        TrackMeDatabaseUtilsImpl.getInstance().updateRouteTrack(route);
    }

    public boolean isRecording() {
        return recordingState == RecordingState.RESUMED || recordingState == RecordingState.STARTED || routeId != PreferenceUtils.DEFAULT_ROUTE_ID;
    }

    public long startNewTracking() {
        if (isRecording()) {
            Log.d(TAG, "Ignore startNewTracking. Already recording.");
            return -1L;
        }
        return startRouteTracking();
    }


    private long startRouteTracking() {
        long now = System.currentTimeMillis();

        routeStatsManager = new RouteStatsManager(now);
        Route route = new Route(true);
        Uri uriRouteInsert = TrackMeDatabaseUtilsImpl.getInstance().insertRouteTrack(route);
        long insertedRouteId = Long.parseLong(uriRouteInsert.getLastPathSegment());
        PreferenceUtils.setRouteId(this, R.string.route_id_key, insertedRouteId);
        routeId = insertedRouteId;
        route.setRouteId(insertedRouteId);

        TrackMeDatabaseUtilsImpl.getInstance().updateRouteTrack(route);
        insertRouteCheckPoint(RouteTrackCreator.DEFAULT_ROUTE_TRACK_BUILDER);

        startRecording();

        updateRecordingState(routeId, RecordingState.STARTED);
        //mock part

        if (testAllowed) {
            initMockTimer();
        }
        return routeId;
    }

    /**
     * Starts gps.
     */
    private void startGPSTracking() {
        wakeLock = SystemUtils.acquireWakeLock(this, wakeLock);
        startRequestingLocationUpdates();
    }


    private void resumeTracking() {
        Log.d(TAG, "resume the route track");
        if (isResumed()) {
            Log.d(TAG, " already resumed");
            return;
        }
        updateRecordingState(routeId, RecordingState.RESUMED);

        Route pausedRoute = TrackMeDatabaseUtilsImpl.getInstance().getRouteById(routeId);
        if (pausedRoute != null) {
            insertLocation(pausedRoute, mLastLocation, getLastValidLocationForRoute(pausedRoute.getRouteId()));

            Location resumeLoc = new Location(LocationManager.GPS_PROVIDER);
            resumeLoc.setLatitude(LocationUtils.PAUSE_RESUME_LATITUDE);
            resumeLoc.setLongitude(LocationUtils.RESUME_LONGITUDE);
            resumeLoc.setTime(System.currentTimeMillis());

            insertLocation(pausedRoute, resumeLoc, null);

        }
        startRecording();
        //mock part
        if (mockHandler != null) {
            mockHandler.postDelayed(mockRunnable, 1000);
        }
    }

    private void startRecording() {

        mLastLocation = null;
        firstInsert = true;
        isIdle.set(false);
        startGPSTracking();
        sendRouteBroadcast(recordingState == RecordingState.STARTED ? R.string.route_started_broadcast_action : R.string.route_resumed_broadcast_action, routeId);
        showNotification();
    }


    private long insertRoutePoint(RouteTrackCreator routeTrackCreator) {

        if (!isRecording() || isPaused()) {
            return -1L;
        }

        Location location = TrackMeDatabaseUtilsImpl.getInstance().getLastValidLocationForRoute(routeId);
        if (!LocationUtils.isValidLocation(location)) {
            location = routeTrackCreator.getLocation();
        }
        RoutePoint routePoint = new RoutePoint(DEFAULT_ROUTE_POINT_ID, routeId, location, null);
        Uri uri = TrackMeDatabaseUtilsImpl.getInstance().insertRoutePoint(routePoint);
        return Long.parseLong(uri.getLastPathSegment());
    }

    private long insertRouteCheckPoint(RouteTrackCreator routeTrackCreator) {

        if (!isRecording() || isPaused()) {
            return -1L;
        }

        Location location = TrackMeDatabaseUtilsImpl.getInstance().getLastValidLocationForRoute(routeId);
        if (!LocationUtils.isValidLocation(location)) {
            location = routeTrackCreator.getLocation();
        }
        RouteCheckPoint routePoint = new RouteCheckPoint(routeId, routeTrackCreator.getName(), routeTrackCreator.getDescription(), location, null, String.valueOf(Color.BLUE));
        Uri uri = TrackMeDatabaseUtilsImpl.getInstance().insertRouteCheckPoint(routePoint);
        return Long.parseLong(uri.getLastPathSegment());
    }


    private void pauseTracking() {

        Log.d(TAG, "pausing the route track");

        updateRecordingState(routeId, RecordingState.PAUSED);
        Route route = TrackMeDatabaseUtilsImpl.getInstance().getRouteById(routeId);

        if (route != null) {
            insertLocation(route, mLastLocation, getLastValidLocationForRoute(route.getRouteId()));

            Location pauseLoc = new Location(LocationManager.GPS_PROVIDER);
            pauseLoc.setLatitude(LocationUtils.PAUSE_RESUME_LATITUDE);
            pauseLoc.setLongitude(LocationUtils.PAUSE_LONGITUDE);
            pauseLoc.setTime(System.currentTimeMillis());

            insertLocation(route, pauseLoc, null);
        }

        stopRecordingService(false);
    }

    private void stopRouteTracking(boolean stopped) {

        if (!isRecording()) {
            Log.d(TAG, "Ignore endCurrentTrack. Not recording.");
            return;
        }

        long currentRouteId = routeId;

        RecordingState isPaused = recordingState;
        updateRecordingState(PreferenceUtils.DEFAULT_ROUTE_ID, RecordingState.STOPPED);
        //todo update status sometime.
        Route currentRoute = TrackMeDatabaseUtilsImpl.getInstance().getRouteById(currentRouteId);
        if (currentRoute != null) {
            if (isPaused == RecordingState.PAUSED) {
                Location lastValidLocation = TrackMeDatabaseUtilsImpl.getInstance().getLastValidLocationForRoute(currentRouteId);
                if (LocationUtils.isValidLocation(lastValidLocation) && mLastLocation != null && !insertLocation(currentRoute, mLastLocation, lastValidLocation)) {
                    //if somehow the last location is the same as the last inserted,
                    //we still want to update the route's time.
                    //if it is true, it is updated already, does not make sense to update it again.
                    updateRecordingRoute(currentRoute, currentRouteId, false);
                }
            }
        }
        sendRouteBroadcast(stopped ? R.string.route_stopped_broadcast_action : R.string.route_paused_broadcast_action, routeId);
        stopRecordingService(stopped);
    }

    private void stopRecordingService(boolean stopped) {
        mLastLocation = null;
        recordingState = stopped ? RecordingState.STOPPED : RecordingState.PAUSED;

        stopGpsTracking(stopped);
        //mock timer
        Log.d(TAG, "stopping the mock timer");
        if (testAllowed) {
            if (mockHandler != null) {
                mockHandler.removeCallbacks(mockRunnable);
            }
        }
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
        if (isRecording()) {
            if (!isPaused()) {
                Intent intent = IntentUtils.newIntent(this, MainActivity.class).putExtra(Constants.EXTRA_ROUTE_ID, routeId);
                PendingIntent pendingIntent = TaskStackBuilder.create(this).addParentStack(MainActivity.class).addNextIntent(intent).getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                startForegroundService(pendingIntent, R.string.tracking_record_notification);
            } else {
                stopForegroundService();
            }
        }
    }

    private void stopNotifications() {
        stopForegroundService();
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
     * Sends track broadcast.
     *
     * @param actionId the intent action id
     * @param routeId  the track id
     */
    private void sendRouteBroadcast(int actionId, long routeId) {
        Intent intent = new Intent().setAction(getString(actionId)).putExtra(getString(R.string.route_id_broadcast_extra), routeId);
        sendBroadcast(intent, getString(R.string.permission_notification_value));
    }

    protected Location getLastLocation() {
        return mLastLocation;
    }


    protected Location getLastValidLocationForRoute() {
        return TrackMeDatabaseUtilsImpl.getInstance().getLastValidLocationForRoute(routeId);
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
        super.onDestroy();
    }

    private Location getLastValidLocationForRoute(long routeId) {
        return TrackMeDatabaseUtilsImpl.getInstance().getLastValidLocationForRoute(routeId);
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

    private TrackMeApplication getApp() {
        return (TrackMeApplication) TrackMeApplication.getInstance();
    }


    public static class ServiceBinder extends IRecordingService.Stub {

        private static final String ServiceTAG = ServiceBinder.class.getCanonicalName();
        private DeathRecipient deathRecipient;
        private RecordingServiceImpl recordingService;

        public ServiceBinder(RecordingServiceImpl recordingService) {
            this.recordingService = recordingService;
        }

        @Override
        public void linkToDeath(DeathRecipient recipient, int flags) {
            deathRecipient = recipient;
        }

        @Override
        public boolean unlinkToDeath(DeathRecipient recipient, int flags) {
            if (!isBinderAlive()) {
                return false;
            }
            deathRecipient = null;
            return true;
        }

        @Override
        public long startNewRoute() throws RemoteException {
            if (!canContinue()) {
                Log.d(ServiceTAG, "service is null. starting failed.");
                return -1L;
            }
            return recordingService.startNewTracking();
        }

        private boolean canContinue() {
            if (recordingService == null) {
                throw new IllegalStateException("The track recording service has been detached!");
            }
            if (Process.myPid() == Binder.getCallingPid()) {
                return true;
            }
            return false;
        }

        @Override
        public void pauseCurrentRoute() throws RemoteException {
            if (!canContinue()) {
                Log.d(ServiceTAG, "service is null. pausing failed");
                return;
            }
            recordingService.pauseTracking();
        }

        @Override
        public void resumeRecordingService() throws RemoteException {
            if (!canContinue()) {
                Log.d(ServiceTAG, "service is null. resume failed.");
                return;
            }
            recordingService.resumeTracking();
        }

        @Override
        public boolean isPaused() throws RemoteException {
            return canContinue() && recordingService.isPaused();
        }

        @Override
        public boolean isRecording() throws RemoteException {
            return canContinue() && recordingService.isRecording();
        }


        @Override
        public void stopCurrentRoute() throws RemoteException {
            if (!canContinue()) {
                return;
            }
            recordingService.stopRouteTracking(false);
        }

        @Override
        public long getRouteId() throws RemoteException {
            return 0;
        }
    }

}
