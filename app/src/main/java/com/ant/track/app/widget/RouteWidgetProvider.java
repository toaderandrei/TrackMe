package com.ant.track.app.widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;
import android.widget.RemoteViews;

import com.ant.track.app.R;
import com.ant.track.app.activities.RouteDetailsActivity;
import com.ant.track.app.helper.ApiHelper;
import com.ant.track.app.helper.IntentUtils;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.content.factory.TrackMeDatabaseUtils;
import com.ant.track.lib.content.factory.TrackMeDatabaseUtilsImpl;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.lib.stats.RouteStats;
import com.ant.track.lib.stats.StringUtils;

/**
 * App widget provider.
 */
public class RouteWidgetProvider extends AppWidgetProvider {


    private static final int[] ITEM1_IDS = {R.id.widget_item1_label,
            R.id.widget_item1_value, R.id.widget_item1_unit,
            R.id.widget_item1_chronometer};


    private static final int[] ITEM2_IDS = {R.id.track_widget_item2_label,
            R.id.widget_item2_value, R.id.widget_item2_unit,
            R.id.widget_item2_chronometer};

    private static final int[] ITEM3_IDS = {R.id.track_widget_item3_label,
            R.id.widget_item3_value, R.id.widget_item3_unit,
            R.id.widget_item3_chronometer};

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (context.getString(R.string.route_paused_broadcast_action).equals(action)
                || context.getString(R.string.route_resumed_broadcast_action).equals(action)
                || context.getString(R.string.route_started_broadcast_action).equals(action)
                || context.getString(R.string.route_stopped_broadcast_action).equals(action)
                || context.getString(R.string.route_update_broadcast_action).equals(action)) {
            long trackId = intent.getLongExtra(context.getString(R.string.route_id_broadcast_extra), -1L);
            updateAllAppWidgets(context, trackId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        // Need to update all app widgets after phone reboot
        updateAllAppWidgets(context, -1L);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        // Need to update all app widgets after software update
        updateAllAppWidgets(context, -1L);
    }

    @TargetApi(16)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        if (newOptions != null) {
            int newSize = 2;

            int size = ApiHelper.getAppWidget().getAppWidgetSize(appWidgetManager, appWidgetId);
            if (size != newSize) {
                ApiHelper.getAppWidget().setAppWidgetSize(appWidgetManager, appWidgetId, newSize);
                updateAppWidget(context, appWidgetManager, appWidgetId, -1L);
            }
        }
    }

    /**
     * Updates an app widget.
     *
     * @param context          the context
     * @param appWidgetManager the app widget manager
     * @param appWidgetId      the app widget id
     * @param trackId          the track id. -1L to not specify one
     */
    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, long trackId) {

        int size = ApiHelper.getAppWidget().getAppWidgetSize(appWidgetManager, appWidgetId);
        RemoteViews remoteViews = getRemoteViews(context, trackId, size);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }

    /**
     * Updates all app widgets.
     *
     * @param context the context
     * @param trackId track id
     */
    private static void updateAllAppWidgets(Context context, long trackId) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(context, RouteWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, trackId);
        }
    }

    /**
     * Gets the remote views.
     *
     * @param context    the context
     * @param routeId    the track id
     * @param heightSize the layout height size
     */
    private static RemoteViews getRemoteViews(Context context, long routeId, int heightSize) {
        int layout = R.layout.widget_provider_layout;
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), layout);

        // Get the preferences

        long recordingRouteIdFromPrefs = PreferenceUtils.getLong(context, R.string.route_id_key);

        RecordingState recordingState = PreferenceUtils.getRecordingState(context, R.string.recording_state_key, PreferenceUtils.RECORDING_STATE_NOT_STARTED_DEFAULT);

        int item1 = PreferenceUtils.getInt(context, R.string.widget_item1, PreferenceUtils.WIDGET_ITEM1_DEFAULT);
        int item2 = PreferenceUtils.getInt(context, R.string.widget_item2, PreferenceUtils.WIDGET_ITEM2_DEFAULT);

        // Get currentRoute and trip stats

        TrackMeDatabaseUtils trackMeDatabaseUtils = TrackMeDatabaseUtilsImpl.getInstance();
        if (routeId == -1L) {
            routeId = recordingRouteIdFromPrefs;
        }
        Route currentRoute = routeId != -1L ? trackMeDatabaseUtils.getRouteById(routeId) : trackMeDatabaseUtils.getLastRoute();
        RouteStats tripStatistics = currentRoute == null ? null : currentRoute.getRouteStats();
        //this is true for now.
        updateStatisticsContainer(context, remoteViews, currentRoute);
        setItem(context, remoteViews, ITEM1_IDS, item1, tripStatistics, recordingState);
        setItem(context, remoteViews, ITEM2_IDS, item2, tripStatistics, recordingState);

        if (heightSize > 1) {
            int item3 = PreferenceUtils.getInt(context, R.string.widget_item3, PreferenceUtils.WIDGET_ITEM3_DEFAULT);
            setItem(context, remoteViews, ITEM3_IDS, item3, tripStatistics, recordingState);
        }
        return remoteViews;
    }

    /**
     * Sets a widget item.
     *
     * @param context        the context
     * @param remoteViews    the remote view
     * @param ids            the item's ids
     * @param value          the item value
     * @param tripStatistics the trip statistics
     */
    private static void setItem(Context context,
                                RemoteViews remoteViews,
                                int[] ids,
                                int value,
                                RouteStats tripStatistics,
                                RecordingState state) {
        switch (value) {
            case R.string.stats_distance:
                updateDistance(context, remoteViews, ids, tripStatistics);
                break;
            case R.string.stats_total_time:
                updateTotalTime(context, remoteViews, ids, tripStatistics, state);
                break;
            case R.string.stats_average_speed:
                updateAverageSpeed(context, remoteViews, ids, tripStatistics);
                break;
            case R.string.stats_moving_time:
                updateMovingTime(context, remoteViews, ids, tripStatistics);
                break;
            default:
                updateDistance(context, remoteViews, ids, tripStatistics);
                break;

        }
        if (value != 1) {
            remoteViews.setViewVisibility(ids[1], View.VISIBLE);
            remoteViews.setViewVisibility(ids[2], View.VISIBLE);
            remoteViews.setViewVisibility(ids[3], View.GONE);
            remoteViews.setChronometer(ids[3], SystemClock.elapsedRealtime(), null, false);
        }
    }

    /**
     * Updates the statistics container.
     *
     * @param context     the context
     * @param remoteViews the remote views
     * @param route       the route
     */
    private static void updateStatisticsContainer(Context context, RemoteViews remoteViews, Route route) {
        PendingIntent pendingIntent;
        if (route != null) {
            Intent intent = IntentUtils.newIntent(context, RouteDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_VIEW_ROUTE_DETAILS, true);
            intent.putExtra(Constants.EXTRA_ROUTE_ID_KEY, route.getRouteId());
            pendingIntent = TaskStackBuilder.create(context)
                    .addParentStack(RouteDetailsActivity.class).addNextIntent(intent).getPendingIntent(0, 0);
        } else {
            Intent intent = IntentUtils.newIntent(context, RouteDetailsActivity.class);
            intent.putExtra(Constants.EXTRA_VIEW_ROUTE_DETAILS, false);
            pendingIntent = TaskStackBuilder.create(context).addNextIntent(intent).getPendingIntent(0, 0);
        }
        remoteViews.setOnClickPendingIntent(R.id.track_widget_stats_container, pendingIntent);
    }

    /**
     * Updates distance.
     *
     * @param context     the context
     * @param remoteViews the remote views
     * @param ids         the item's ids
     * @param routeStats  the trip statistics
     */
    private static void updateDistance(Context context, RemoteViews remoteViews, int[] ids,
                                       RouteStats routeStats) {
        double totalDistance = routeStats == null ? Double.NaN : routeStats.getTotalDistance();
        String[] totalDistanceParts = StringUtils.getDistanceParts(context, totalDistance);
        if (totalDistanceParts[0] == null) {
            totalDistanceParts[0] = context.getString(R.string.value_unknown);
        }
        remoteViews.setTextViewText(ids[0], context.getString(R.string.stats_distance));
        remoteViews.setTextViewText(ids[1], totalDistanceParts[0]);
        remoteViews.setTextViewText(ids[2], totalDistanceParts[1]);
    }

    /**
     * Updates total time.
     *
     * @param context        the context
     * @param remoteViews    the remote views
     * @param ids            the item's ids
     * @param tripStatistics the trip statistics
     */
    private static void updateTotalTime(Context context, RemoteViews remoteViews, int[] ids,
                                        RouteStats tripStatistics, RecordingState state) {
        if ((state == RecordingState.STARTED || state == RecordingState.RESUMED) && tripStatistics != null) {
            long time = tripStatistics.getTotalTime() + System.currentTimeMillis()
                    - tripStatistics.getStopTime();
            remoteViews.setChronometer(ids[3], SystemClock.elapsedRealtime() - time, null, true);
            remoteViews.setViewVisibility(ids[1], View.GONE);
            remoteViews.setViewVisibility(ids[2], View.GONE);
            remoteViews.setViewVisibility(ids[3], View.VISIBLE);
        } else {
            remoteViews.setChronometer(ids[3], SystemClock.elapsedRealtime(), null, false);
            remoteViews.setViewVisibility(ids[1], View.VISIBLE);
            remoteViews.setViewVisibility(ids[2], View.GONE);
            remoteViews.setViewVisibility(ids[3], View.GONE);

            String totalTime = tripStatistics == null ? context.getString(R.string.value_unknown)
                    : StringUtils.formatElapsedTime(tripStatistics.getTotalTime());
            remoteViews.setTextViewText(ids[0], context.getString(R.string.stats_total_time));
            remoteViews.setTextViewText(ids[1], totalTime);
        }
    }

    /**
     * Updates average speed.
     *
     * @param context     the context
     * @param remoteViews the remote views
     * @param ids         the item's ids
     * @param routeStats  the trip statistics
     */
    private static void updateAverageSpeed(Context context,
                                           RemoteViews remoteViews,
                                           int[] ids,
                                           RouteStats routeStats) {
        String averageSpeedLabel = context.getString(R.string.stats_average_speed);
        remoteViews.setTextViewText(ids[0], averageSpeedLabel);

        Double speed = routeStats == null ? Double.NaN : routeStats.getAvgSpeed();
        String[] speedParts = StringUtils.getSpeedParts(context, speed);

        if (speedParts[0] == null) {
            speedParts[0] = context.getString(R.string.value_unknown);
        }

        remoteViews.setTextViewText(ids[1], speedParts[0]);
        remoteViews.setTextViewText(ids[2], speedParts[1]);
    }

    /**
     * Updates moving time.
     *
     * @param context     the context
     * @param remoteViews the remote views
     * @param ids         the item's ids
     * @param routeStats  the trip statistics
     */
    private static void updateMovingTime(Context context, RemoteViews remoteViews, int[] ids, RouteStats routeStats) {
        String movingTime = routeStats == null ? context.getString(R.string.value_unknown) : StringUtils.formatElapsedTime(routeStats.getMovingTime());
        remoteViews.setTextViewText(ids[0], context.getString(R.string.stats_moving_time));
        remoteViews.setTextViewText(ids[1], movingTime);
        remoteViews.setViewVisibility(ids[2], View.GONE);
    }
}
