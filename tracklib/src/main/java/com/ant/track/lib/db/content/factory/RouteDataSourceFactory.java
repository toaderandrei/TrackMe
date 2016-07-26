package com.ant.track.lib.db.content.factory;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.ant.track.lib.R;
import com.ant.track.lib.db.content.datasource.DataSourceManager;
import com.ant.track.lib.db.content.datasource.RouteDataListener;
import com.ant.track.lib.db.content.datasource.RouteType;
import com.ant.track.lib.db.content.publisher.DataContentPublisher;
import com.ant.track.lib.db.content.publisher.DataContentPublisherImpl;
import com.ant.track.lib.db.content.publisher.RouteDataSourceListener;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Factory class that updates all the listeners registered to the data source class.
 */
public class RouteDataSourceFactory implements RouteDataSourceListener {

    private Handler handler;

    private long loadedRouteId;
    private long recordingRouteId;
    private RecordingState recordingState;

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

    public RouteDataSourceFactory(Context context) {
        this.context = context;
        handler = new Handler();
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
            notifyRoutePointsUpdateInternal(dataListeners);
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
            notifyRoutePointsUpdateInternal(dataListeners);
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
                if (routeId == loadedRouteId) {
                    Log.i(TAG, "Not reloading track " + routeId);
                    return;
                }
                loadedRouteId = routeId;
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
                notifyRoutePointsUpdateInternal(contentPublisher.getListeners());
            }
        });
    }

    private void notifyRoutePointsUpdateInternal(Set<RouteDataListener> dataListeners) {

    }

    private void notifyRouteCheckPointUpdateInternal(Set<RouteDataListener> dataListeners) {

    }

    private void notifyRouteUpdateInternal(Set<RouteDataListener> dataListeners) {

    }


    @Override
    public void notifyPreferenceChanged(final String key) {
        runAsync(new Runnable() {
                     @Override
                     public void run() {

                         if (!TextUtils.isEmpty(key)) {

                             //gps accuracy
                             if (TextUtils.equals(key, PreferenceUtils.getKey(context, R.string.recording_gps_accuracy_key))) {
                                 recordingGpsAccuracy = PreferenceUtils.getInt(context,
                                         R.string.recording_gps_accuracy_key,
                                         PreferenceUtils.RECORDING_GPS_ACCURACY_DEFAUL);
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
    public boolean iSSelectedRouteRecording() {
        return loadedRouteId == recordingRouteId && recordingRouteId != PreferenceUtils.DEFAULT_ROUTE_ID;
    }

    /**
     * Returns true if the selected track is paused.
     */
    public boolean isSelectedRoutePaused() {
        return loadedRouteId == recordingRouteId && recordingState == RecordingState.PAUSED;
    }
}
