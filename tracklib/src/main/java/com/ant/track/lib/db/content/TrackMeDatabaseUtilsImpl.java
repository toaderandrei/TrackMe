package com.ant.track.lib.db.content;

import android.content.ContentValues;
import android.location.Location;
import android.net.Uri;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.db.TrackMeContract;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RoutePoint;
import com.ant.track.lib.stats.RouteStats;
import com.ant.track.lib.utils.LocationUtils;

/**
 * Track me database utility class. It deals with insertion, deletion,
 * updating and deleting of data from db.
 */
public class TrackMeDatabaseUtilsImpl implements TrackMeDatabaseUtils {

    private static TrackMeDatabaseUtils instance;

    public static TrackMeDatabaseUtils getInstance() {
        if (instance == null) {
            instance = new TrackMeDatabaseUtilsImpl();
        }
        return instance;
    }

    public Route getRouteById(long routeId) {
        Route route = new Route();


        return route;
    }

    @Override
    public Uri insertRoutePoint(long routeId, Location location) {
        return getApp().getContentResolver().insert(TrackMeContract.RoutePointEntry.CONTENT_URI,
                createContentValues(routeId, location));
    }

    @Override
    public Location getLastValidLocation(long routeId) {
        return null;
    }

    @Override
    public Location getLastValidRouteTrack(long routeId) {
        return null;
    }

    @Override
    public Uri insertRouteTrack(Route route) {
        return getApp().getContentResolver().insert(TrackMeContract.RouteEntry.CONTENT_URI, createContentValues(route));
    }

    @Override
    public void deleteRouteTrack(int id) {

    }

    @Override
    public void updateRouteTrack(Route route) {

    }

    @Override
    public void updateRouteTrack(int id, ContentValues values) {

    }

    @Override
    public void insertRoutePoint(RoutePoint routePoint) {

    }

    @Override
    public void deleteRoutePoint(int id) {

    }

    @Override
    public void updateRoutePoint(int id, ContentValues values) {

    }

    @Override
    public void updateRoutePoint(RoutePoint routePoint) {

    }


    private ContentValues createContentValues(long routeId, Location location) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TrackMeContract.RoutePointEntry.ROUTE_ID, routeId);
        contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LAT, LocationUtils.getLatitude1E6FromDouble(location.getLatitude()));
        contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LONG, LocationUtils.getLongitude1E6FromDouble(location.getLongitude()));

        long time;
        if (location.getTime() == 0) {
            time = System.currentTimeMillis();
        } else {
            time = location.getTime();
        }
        contentValues.put(TrackMeContract.RoutePointEntry.TIME, time);

        if (location.hasSpeed()) {
            contentValues.put(TrackMeContract.RoutePointEntry.SPEED, location.getSpeed());
        }

        if (location.hasAccuracy()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_ACCURACY, location.getAccuracy());
        }

        if (location.hasAltitude()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_ALT, location.getAltitude());
        }

        if (location.hasBearing()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_BEARING, location.getBearing());
        }

        return contentValues;
    }

    private ContentValues createContentValues(Route route) {
        ContentValues contentValues = new ContentValues();
        RouteStats routeStats = route.getRouteStats();
        if (route.getRouteId() >= 0) {
            contentValues.put(TrackMeContract.RouteEntry._ID, route.getRouteId());
        }

        contentValues.put(TrackMeContract.RouteEntry.TOTAL_TIME, routeStats.getTotalDuration());
        //speed part
        contentValues.put(TrackMeContract.RouteEntry.AVG_SPEED, routeStats.getAvgSpeed());
        contentValues.put(TrackMeContract.RouteEntry.MIN_SPEED, routeStats.getMinSpeed());
        contentValues.put(TrackMeContract.RouteEntry.MAX_SPEED, routeStats.getMaxSpeed());
        contentValues.put(TrackMeContract.RouteEntry.MIN_LONGITUDE, LocationUtils.getLongitude1E6FromDouble(routeStats.getLongitudeMin()));
        contentValues.put(TrackMeContract.RouteEntry.MAX_LONGITUDE, LocationUtils.getLongitude1E6FromDouble(routeStats.getLongitudeMax()));

        contentValues.put(TrackMeContract.RouteEntry.MIN_LATITUDE, LocationUtils.getLatitude1E6FromDouble(routeStats.getLatitudeMin()));
        contentValues.put(TrackMeContract.RouteEntry.MAX_LATITUDE, LocationUtils.getLatitude1E6FromDouble(routeStats.getLatitudeMax()));
        contentValues.put(TrackMeContract.RouteEntry.MIN_ELEVATION, LocationUtils.getElevation1E6FromDouble(routeStats.getElevationMin()));
        contentValues.put(TrackMeContract.RouteEntry.MAX_ELEVATION, LocationUtils.getElevation1E6FromDouble(routeStats.getElevationMax()));
        contentValues.put(TrackMeContract.RouteEntry.ELEVATION_GAIN, routeStats.getTotalElevationGain());

        contentValues.put(TrackMeContract.RouteEntry.NAME, route.getRouteName());
        contentValues.put(TrackMeContract.RouteEntry.NUM_ROUTE_POINTS, route.getNumberOfPoints());
        contentValues.put(TrackMeContract.RouteEntry.TOTAL_DISTANCE, routeStats.getTotalDistance());
        contentValues.put(TrackMeContract.RouteEntry.TOTAL_TIME, routeStats.getTotalDuration());
        contentValues.put(TrackMeContract.RouteEntry.START_POINT_ID, route.getStartPointId());
        contentValues.put(TrackMeContract.RouteEntry.STOP_POINT_ID, route.getStopPointId());
        contentValues.put(TrackMeContract.RouteEntry.START_TIME, routeStats.getStartTime());
        contentValues.put(TrackMeContract.RouteEntry.STOP_TIME, routeStats.getStopTime());

        return contentValues;
    }

    private TrackLibApplication getApp() {
        return TrackLibApplication.getInstance();
    }

    public static void reset() {
        instance = null;
    }
}
