package com.ant.track.lib.db.content.factory;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;

import com.ant.track.lib.R;
import com.ant.track.lib.controller.RouteType;
import com.ant.track.lib.db.content.datasource.DataSourceManager;
import com.ant.track.lib.db.content.datasource.RouteDataListener;
import com.ant.track.lib.db.content.publisher.DataContentPublisher;
import com.ant.track.lib.db.content.publisher.DataContentPublisherImpl;
import com.ant.track.lib.db.content.publisher.RouteDataSourceListener;
import com.ant.track.lib.prefs.PreferenceUtils;

import java.util.EnumSet;

/**
 * Factory class that updates all the listeners registered to the data source class.
 */
public class RouteDataSourceFactory implements RouteDataSourceListener {

    private Handler handler;

    private static final String TAG = RouteDataSourceFactory.class.getCanonicalName();
    private boolean isNotifying;
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

    public void startObservers() {
        handler = new Handler();
        dataSourceManager = new DataSourceManager(handler, this);
        isNotifying = true;
        handlerThread = new HandlerThread("DataSource AsyncThread");
        handlerThread.start();
        bgHandler = new Handler(handlerThread.getLooper());
    }

    public void stopObservers() {
        isNotifying = false;
        handlerThread.quit();
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

    private void loadDataForListener(final RouteDataListener listener) {
        EnumSet<RouteType> types = contentPublisher.getRouteTypes(listener);

        if (types.contains(RouteType.PREFERENCE)) {
            listener.onRecordingDistanceIntervalChanged(maxRecordingDistance);
            listener.onRecordingGpsAccuracyChanged(recordingGpsAccuracy);
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

    }

    @Override
    public void notifyRouteCheckPointUpdate() {

    }

    @Override
    public void notifyRoutePointsUpdate() {

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
                             //
                         }
                     }

                 }
        );
    }
}
