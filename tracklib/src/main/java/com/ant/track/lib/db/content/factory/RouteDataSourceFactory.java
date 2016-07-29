package com.ant.track.lib.db.content.factory;

import android.content.Context;
import android.database.Cursor;
import android.location.Location;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.ant.track.lib.R;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.db.content.datasource.DataSourceManager;
import com.ant.track.lib.db.content.datasource.RouteDataListener;
import com.ant.track.lib.db.content.datasource.RouteType;
import com.ant.track.lib.db.content.publisher.DataContentPublisher;
import com.ant.track.lib.db.content.publisher.DataContentPublisherImpl;
import com.ant.track.lib.db.content.publisher.RouteDataSourceListener;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.lib.utils.LocationUtils;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Factory class that updates all the listeners registered to the data source class.
 */
public class RouteDataSourceFactory implements RouteDataSourceListener {

    private Handler handler;

    private long currentRouteId;
    private RecordingState recordingState;
    public static final int TARGET_DISPLAYED_ROUTE_POINTS = 5000;

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
    private long lastSeenLocationId;
    private long recordingRouteId;

    public RouteDataSourceFactory(Context context) {
        this.context = context;
        handler = new Handler();
        targetNumPoints = TARGET_DISPLAYED_ROUTE_POINTS;
        contentPublisher = new DataContentPublisherImpl();
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
                    loadAll();
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

                if (routeDataListener == null && types == null) {
                    Log.i(TAG, "should throw an exception.");
                    return;
                }
                contentPublisher.registerListener(routeDataListener, types);
                dataSourceManager.updateListeners(contentPublisher.getRouteTypes(routeDataListener));
                loadDataForListener(routeDataListener);
            }
        });

    }

    private void loadAll() {
        if (contentPublisher.getRouteTypesCount() == 0) {
            return;
        }

        Set<RouteDataListener> dataListeners = contentPublisher.getListeners();

        for (RouteDataListener listener : contentPublisher.getListeners()) {
            listener.onRecordingGpsAccuracyChanged(recordingGpsAccuracy);
            listener.onRecordingDistanceIntervalChanged(minRecordingDistance);
            listener.onRecordingMaxDistanceChanged(maxRecordingDistance);
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
            listener.onRecordingDistanceIntervalChanged(minRecordingDistance);
            listener.onRecordingGpsAccuracyChanged(recordingGpsAccuracy);
        }
        if (types.contains(RouteType.ROUTE)) {
            notifyRouteUpdateInternal(dataListeners);
        }

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
                if (listener == null) {
                    Log.i(TAG, "should throw an exception.");
                    return;
                }
                contentPublisher.unregisterListener(listener);
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
        if (runnable != null) {
            bgHandler.post(runnable);
        }
    }

    @Override
    public void notifyRouteUpdate() {
        runAsync(new Runnable() {
            @Override
            public void run() {
                notifyRouteUpdateInternal(contentPublisher.getListeners());
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
                if (routeId == currentRouteId) {
                    Log.i(TAG, "Not reloading track " + routeId);
                    return;
                }
                currentRouteId = routeId;
                loadAll();
            }
        });
    }

    @Override
    public void notifyRouteCheckPointUpdate() {
        runAsync(new Runnable() {
            @Override
            public void run() {
                notifyRouteCheckPointUpdateInternal(contentPublisher.getListeners());
            }
        });
    }

    @Override
    public void notifyRoutePointsUpdate() {
        runAsync(new Runnable() {
            @Override
            public void run() {
                notifyRoutePointsUpdateInternal(true, contentPublisher.getListeners());
            }
        });
    }

    private void notifyRoutePointsUpdateInternal(boolean update, Set<RouteDataListener> routePointsDataListeners) {

        if (routePointsDataListeners == null || routePointsDataListeners.isEmpty()) {
            Log.d(TAG, "listeners empty, nothing to update.");
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
            locationIterator = TrackMeDatabaseUtilsImpl.getInstance().getRoutePointsIterator(currentRouteId, localLastSeenLocationId);

            while (locationIterator.hasNext()) {
                Location location = locationIterator.next();
                long locationId = locationIterator.getLocationId();

                // Stop if past the last wanted point
                if (maxPointId != -1L && locationId > maxPointId) {
                    break;
                }
                int insertedPoints = Math.max(0, getInsertedPoints(currentRouteId));
                boolean updateInsertedPoints = insertedPoints <= targetNumPoints;

                if (!LocationUtils.isValidLocation(location)) {
                    for (RouteDataListener routeDataListener : routePointsDataListeners) {
                        Location validLoc = TrackMeDatabaseUtilsImpl.getInstance().getLastValidLocationForRoute(currentRouteId);
                        routeDataListener.addPendingLocation(validLoc);
                        routeDataListener.addLocationToQueue(location);
                        includeNextPoint = true;
                    }
                } else {
                    // Also include the last point if the selected track is not recording.
                    if (includeNextPoint || updateInsertedPoints || (locationId == lastTrackPointId && !isSelectedRouteRecording())) {
                        includeNextPoint = false;
                        for (RouteDataListener trackDataListener : routePointsDataListeners) {
                            trackDataListener.addPendingLocation(location);
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


    private int getInsertedPoints(long routeid) {

        if (routeid < 0) {
            return -1;
        }

        Cursor cursor = null;
        try {
            cursor = TrackMeDatabaseUtilsImpl.getInstance().getRouteCursor(routeid);
            if (cursor != null && cursor.isBeforeFirst() && cursor.moveToNext()) {
                Route route = TrackMeDatabaseUtilsImpl.getInstance().createRouteFromCursor(cursor);
                if (route != null) {
                    return route.getNumberOfPoints();
                }


            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1;
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
                            routeDataListener.addRouteCheckPointToMap(routeCheckPoint);
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
            routeDataListener.onNewRouteUpdate(route);
        }
    }


    @Override
    public void notifyPreferenceChanged(final String key) {
        runAsync(new Runnable() {
                     @Override
                     public void run() {

                         if (!TextUtils.isEmpty(key)) {
                             if (TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.route_id_key))) {
                                 recordingRouteId = PreferenceUtils.getLong(context,
                                         R.string.route_id_key,
                                         PreferenceUtils.DEFAULT_ROUTE_ID);
                             }
                             //gps accuracy
                             if (TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.recording_gps_accuracy_key))) {
                                 recordingGpsAccuracy = PreferenceUtils.getInt(context,
                                         R.string.recording_gps_accuracy_key,
                                         PreferenceUtils.RECORDING_GPS_ACCURACY_DEFAULT);
                             }

                             //distance interval
                             if (TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.recording_distance_interval_key))) {
                                 minRecordingDistance = PreferenceUtils.getInt(context,
                                         R.string.recording_distance_interval_key,
                                         PreferenceUtils.RECORDING_DISTANCE_DEFAULT);
                             }

                             //distance interval
                             if (TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.max_recording_distance_key))) {
                                 maxRecordingDistance = PreferenceUtils.getInt(context,
                                         R.string.recording_distance_interval_key,
                                         PreferenceUtils.DEFAULT_MAX_RECORDING_DISTANCE);
                             }

                             if (TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.recording_state_key))) {
                                 recordingState = PreferenceUtils.getRecordingState(context,
                                         R.string.recording_state_key,
                                         PreferenceUtils.RECORDING_STATE_PAUSED_DEFAULT);
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
