package com.ant.track.lib.content.factory;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.ant.track.lib.R;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.content.datasource.DataSourceManager;
import com.ant.track.lib.content.datasource.RouteDataListener;
import com.ant.track.lib.content.datasource.RouteType;
import com.ant.track.lib.content.publisher.DataContentPublisher;
import com.ant.track.lib.content.publisher.DataContentPublisherImpl;
import com.ant.track.lib.content.publisher.RouteDataSourceListener;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.lib.utils.LocationUtils;
import com.google.android.gms.maps.GoogleMap;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Factory class that updates all the listeners registered to the data source class.
 * This class initializes the DataSource manager that registers
 * all the observers for the desired Uri and when an update arrives
 * forwards it via RouteDataSource listener callbacks. Next, we forward it to the
 * Ui listeners. All the requests coming from db are process in a background thread.
 */
public class RouteDataSourceFactory implements RouteDataSourceListener {

    private Handler handler;

    private long currentRouteId;
    private RecordingState recordingState;
    public static final int TARGET_DISPLAYED_ROUTE_POINTS = 5000;

    private static final Object lock = new Object();
    private int numLoadedPoints;

    private static final String TAG = RouteDataSourceFactory.class.getCanonicalName();
    private boolean isStarted;
    private DataContentPublisher contentPublisher;
    private HandlerThread handlerThread;
    private Handler bgHandler;

    private Context context;
    private DataSourceManager dataSourceManager;
    private int recordingGpsAccuracy;
    private int minRecordingDistance;
    private int maxRecordingDistance;
    private int targetNumPoints;
    private long lastSeenLocationId = -1L;
    private int mapType = GoogleMap.MAP_TYPE_NORMAL;
    private long recordingRouteId;

    public RouteDataSourceFactory(Context context) {
        this.context = context;
        handler = new Handler();
        targetNumPoints = TARGET_DISPLAYED_ROUTE_POINTS;
        contentPublisher = new DataContentPublisherImpl();
        reset();
    }

    public void start() {
        handler = new Handler();
        dataSourceManager = new DataSourceManager(handler, this);
        isStarted = true;
        handlerThread = new HandlerThread("DataSource AsyncThread");
        handlerThread.start();
        bgHandler = new Handler(handlerThread.getLooper());
        notifyPreferenceChanged(null);
        runAsync(new Runnable() {
            @Override
            public void run() {
                if (dataSourceManager != null) {
                    dataSourceManager.updateListeners(contentPublisher.getAllRouteTypes());
                    try {
                        loadAll();
                    } catch (Exception exc) {
                        Log.e(TAG, "exception in loading all:" + exc);
                    }
                }
            }
        });
    }

    public void stop() {
        if (!isStarted) {
            Log.e(TAG, "already stopped or not yet started.");
            return;
        }
        isStarted = false;
        if (handlerThread != null) {
            handlerThread.quit();
            handlerThread = null;
        }
        if (dataSourceManager != null) {
            dataSourceManager.clear();
            dataSourceManager = null;
        }
        handler = null;
    }

    public void registerListeners(final RouteDataListener routeDataListener, final EnumSet<RouteType> types) {

        runAsync(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    if (routeDataListener == null && types == null) {
                        Log.i(TAG, "should throw an exception.");
                        return;
                    }
                    contentPublisher.registerListener(routeDataListener, types);
                    dataSourceManager.updateListeners(contentPublisher.getRouteTypes(routeDataListener));
                    loadDataForListener(routeDataListener);
                }
            }
        });

    }

    private void loadAll() {
        reset();
        //temporary hack
        //todo please fix the issues
        notifyPreferenceChanged(null);
        if (contentPublisher.getRouteTypesCount() == 0) {
            return;
        }

        Set<RouteDataListener> dataListeners = contentPublisher.getListeners();

        for (RouteDataListener listener : contentPublisher.getListenersByType(RouteType.PREFERENCE)) {
            listener.onRecordingGpsAccuracyChanged(recordingGpsAccuracy);
            listener.onRecordingDistanceIntervalChanged(minRecordingDistance);
            listener.onRecordingMaxDistanceChanged(maxRecordingDistance);
            listener.onMapTypeChanged(mapType);
        }

        for (RouteDataListener listener : contentPublisher.getListenersByType(RouteType.ROUTE_RESAMPLE_POINTS)) {
            listener.clearPoints();
        }


        if (contentPublisher.getAllRouteTypes().contains(RouteType.ROUTE)) {
            notifyRouteUpdateInternal(dataListeners);
        }

        if (contentPublisher.getAllRouteTypes().contains(RouteType.ROUTE_POINT)) {
            notifyRoutePointsUpdateInternal(true, dataListeners);
        }

        if (contentPublisher.getAllRouteTypes().contains(RouteType.ROUTE_CHECK_POINT)) {
            notifyRouteCheckPointUpdateInternal(dataListeners);
        }
    }

    private void loadDataForListener(final RouteDataListener listener) {
        EnumSet<RouteType> types = contentPublisher.getRouteTypes(listener);
        Set<RouteDataListener> dataListeners = Collections.singleton(listener);


        if (types.contains(RouteType.PREFERENCE)) {
            listener.onRecordingGpsAccuracyChanged(recordingGpsAccuracy);
            listener.onRecordingDistanceIntervalChanged(minRecordingDistance);
            listener.onRecordingMaxDistanceChanged(maxRecordingDistance);
            listener.onMapTypeChanged(mapType);
        }
        if (types.contains(RouteType.ROUTE)) {
            notifyRouteUpdateInternal(dataListeners);
        }

        listener.clearPoints();

        if (types.contains(RouteType.ROUTE_CHECK_POINT)) {
            notifyRouteCheckPointUpdateInternal(dataListeners);
        }

        if (types.contains(RouteType.ROUTE_POINT)) {
            notifyRoutePointsUpdateInternal(true, dataListeners);
        }
    }

    public void unregisterListener(final RouteDataListener listener) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {

                    if (listener == null) {
                        Log.i(TAG, "should throw an exception.");
                        return;
                    }
                    contentPublisher.unregisterListener(listener);
                }
            }
        });
    }

    public void unregisterAll() {
        runAsync(new Runnable() {
            @Override
            public void run() {
                contentPublisher.clearAll();
            }
        });
    }

    private void runAsync(final Runnable runnable) {
        if (runnable != null && bgHandler != null) {
            bgHandler.post(runnable);
        }
    }

    @Override
    public void notifyRouteUpdate() {
        runAsync(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    notifyRouteUpdateInternal(contentPublisher.getListenersByType(RouteType.ROUTE));
                }
            }
        });
    }


    /**
     * Loads a track.
     *
     * @param routeId the track id
     */
    public void loadRouteById(final long routeId) {
        runAsync(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {

                    if (routeId == currentRouteId) {
                        Log.i(TAG, "Not reloading track " + routeId);
                        return;
                    }
                    currentRouteId = routeId;
                    try {
                        loadAll();
                    } catch (Exception exc) {
                        Log.e(TAG, "exception in loading all:" + exc);
                    }
                }
            }
        });
    }

    @Override
    public void notifyRouteCheckPointUpdate() {
        runAsync(new Runnable() {
            @Override
            public void run() {
                notifyRouteCheckPointUpdateInternal(contentPublisher.getListenersByType(RouteType.ROUTE_CHECK_POINT));
            }
        });
    }

    @Override
    public void notifyRoutePointsUpdate() {
        runAsync(new Runnable() {
            @Override
            public void run() {
                synchronized (lock) {
                    notifyRoutePointsUpdateInternal(true, contentPublisher.getListenersByType(RouteType.ROUTE_POINT));
                }
            }
        });
    }

    private void notifyRoutePointsUpdateInternal(boolean update, Set<RouteDataListener> routePointsDataListeners) {

        if (routePointsDataListeners == null || routePointsDataListeners.isEmpty()) {
            Log.d(TAG, "listeners empty, nothing to update.");
            reset();
            return;
        }

        if (update && numLoadedPoints >= targetNumPoints) {
            // Reload and resample the route track.
            for (RouteDataListener listener : routePointsDataListeners) {
                listener.clearPoints();
            }
        }

        int localNumLoadedPoints = update ? numLoadedPoints : 0;
        long localLastSeenLocationId = update ? lastSeenLocationId : -1L;
        long maxPointId = update ? -1L : lastSeenLocationId;
        long lastTrackPointId = TrackMeDatabaseUtilsImpl.getInstance().getLastValidPointId(currentRouteId);
        boolean includeNextPoint = false;
        LocationIterator locationIterator = null;

        try {
            locationIterator = TrackMeDatabaseUtilsImpl.getInstance().getRoutePointsIterator(currentRouteId, localLastSeenLocationId + 1);

            while (locationIterator.hasNext()) {
                Location location = locationIterator.next();
                long locationId = locationIterator.getLocationId();

                // Stop if past the last wanted point
                if (maxPointId != -1L && locationId > maxPointId) {
                    break;
                }
                int insertedPointsInDb = TrackMeDatabaseUtilsImpl.getInstance().getInsertedPoints(currentRouteId);
                int insertedPoints = Math.max(0, insertedPointsInDb);
                boolean updateInsertedPoints = insertedPoints <= targetNumPoints;

                if (!LocationUtils.isValidLocation(location)) {
                    for (RouteDataListener routeDataListener : routePointsDataListeners) {
                        Location validLoc = TrackMeDatabaseUtilsImpl.getInstance().getLastValidLocationForRoute(currentRouteId);
                        routeDataListener.addLocationToMap(validLoc);
                        routeDataListener.addLocationToQueue(location);
                        includeNextPoint = true;
                    }
                } else {
                    // Also include the last point if the selected track is not recording.
                    if (includeNextPoint || updateInsertedPoints || (locationId == lastTrackPointId && !isSelectedRouteRecording())) {
                        includeNextPoint = false;
                        for (RouteDataListener trackDataListener : routePointsDataListeners) {
                            trackDataListener.addLocationToMap(location);
                        }
                    } else {
                        for (RouteDataListener routeDataListener : routePointsDataListeners) {
                            routeDataListener.setLastLocation(location);
                        }
                    }
                }

                localNumLoadedPoints++;
                localLastSeenLocationId = locationId;
            }
        } finally {
            if (locationIterator != null) {
                locationIterator.close();
            }
        }

        if (update) {
            numLoadedPoints = localNumLoadedPoints;
            lastSeenLocationId = localLastSeenLocationId;
        }

        for (RouteDataListener listener : routePointsDataListeners) {
            listener.onNewRoutePointUpdateDone();
        }

    }

    private void reset() {
        numLoadedPoints = 0;
        lastSeenLocationId = -1L;
    }

    private void notifyRouteCheckPointUpdateInternal(Set<RouteDataListener> dataListeners) {
        if (dataListeners.isEmpty()) {
            return;
        }

        Cursor cursor = null;
        try {

            cursor = TrackMeDatabaseUtilsImpl.getInstance().getNewRouteCheckPointsCursor(currentRouteId, -1L, Constants.DEFAULT_MAX_NUMBER_OF_POINTS);
            //check location and so on.
            if (cursor != null && cursor.isBeforeFirst()) {
                while (cursor.moveToNext()) {
                    RouteCheckPoint routeCheckPoint = TrackMeDatabaseUtilsImpl.getInstance().getRouteCheckPointFromCursor(cursor);
                    if (!LocationUtils.isValidLocation(routeCheckPoint.getLocation())) {
                        continue;
                        //todo think of something better.
                    } else {
                        for (RouteDataListener routeDataListener : dataListeners) {
                            routeDataListener.addNewRouteCheckPoint(routeCheckPoint);
                        }

                    }
                }
            }

        } catch (Exception exc) {
            Log.d(TAG, "exception in getting the route points cursor");
            cursor = null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        for (RouteDataListener listener : dataListeners) {
            listener.onNewRouteCheckPointUpdate();
        }
    }

    private void notifyRouteUpdateInternal(Set<RouteDataListener> dataListeners) {
        Route route = TrackMeDatabaseUtilsImpl.getInstance().getRouteById(currentRouteId);
        for (RouteDataListener routeDataListener : dataListeners) {
            if (route != null) {
                routeDataListener.onNewRouteUpdate(route);
            }
        }
    }


    @Override
    public void notifyPreferenceChanged(final String key) {
        runAsync(new Runnable() {
                     @Override
                     public void run() {

                         synchronized (lock) {

                             if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.route_id_key))) {
                                 recordingRouteId = PreferenceUtils.getLong(context,
                                         R.string.route_id_key,
                                         PreferenceUtils.DEFAULT_ROUTE_ID);
                             }
                             //gps accuracy
                             if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.recording_gps_accuracy_key))) {
                                 recordingGpsAccuracy = PreferenceUtils.getInt(context,
                                         R.string.recording_gps_accuracy_key,
                                         PreferenceUtils.RECORDING_GPS_ACCURACY_DEFAULT);
                             }

                             //distance interval
                             if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.recording_distance_interval_key))) {
                                 minRecordingDistance = PreferenceUtils.getInt(context,
                                         R.string.recording_distance_interval_key,
                                         PreferenceUtils.RECORDING_DISTANCE_DEFAULT);
                             }

                             //distance interval
                             if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.max_recording_distance_key))) {
                                 maxRecordingDistance = PreferenceUtils.getInt(context,
                                         R.string.recording_distance_interval_key,
                                         PreferenceUtils.DEFAULT_MAX_RECORDING_DISTANCE);
                             }

                             if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.recording_state_key))) {
                                 recordingState = PreferenceUtils.getRecordingState(context,
                                         R.string.recording_state_key,
                                         PreferenceUtils.RECORDING_STATE_NOT_STARTED_DEFAULT);
                             }

                             if (key == null || TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.map_type_key))) {
                                 mapType = PreferenceUtils.getInt(context, R.string.map_type_key, PreferenceUtils.MAP_TYPE_DEFAULT);
                                 if (mapType >= 0) {
                                     for (RouteDataListener routeDataListener :
                                             contentPublisher.getListenersByType(RouteType.PREFERENCE)) {
                                         routeDataListener.onMapTypeChanged(mapType);
                                     }
                                 }
                             }
                             //
                         }

                     }
                 }
        );
    }

    /**
     * Returns true if the selected track is recording.
     */
    public boolean isSelectedRouteRecording() {
        return currentRouteId == recordingRouteId && currentRouteId != PreferenceUtils.DEFAULT_ROUTE_ID;
    }

    /**
     * Returns true if the selected track is paused.
     */
    public boolean isSelectedRoutePaused() {
        return currentRouteId == recordingRouteId && recordingState == RecordingState.PAUSED;
    }
}
