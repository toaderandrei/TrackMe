package com.ant.track.lib.utils;

import android.content.ContentValues;

import com.ant.track.lib.db.provider.TrackMeContract;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RoutePoint;
import com.ant.track.lib.stats.RouteStats;

/**
 * Utility class for the database.
 */
public class TestDbUtils {


    private static final long now = System.currentTimeMillis();
    private static final long stop = now + 1000;
    private static final long start = now + 100;
    private static final float DEFAULT_AVG_SPEED = 10.20f;

    public static ContentValues getRouteContentValues(String name) {
        RouteStats routeStats = getRandomStats();
        return getRouteContentValues(-1, name, routeStats);
    }

    public static ContentValues getRouteContentValues(long routeid, String name) {
        RouteStats routeStats = getRandomStats();
        return getRouteContentValues(routeid, name, routeStats);
    }

    public static ContentValues getRouteContentValues(long id, String name,
                                                      RouteStats localStats) {
        Route route = new Route();
        route.setRouteId(id);
        route.setRouteName(name);
        ContentValues contentValues = new ContentValues();
        if (route.getRouteId() >= 0) {
            contentValues.put(TrackMeContract.RouteEntry._ID, route.getRouteId());
        }

        contentValues.put(TrackMeContract.RouteEntry.TOTAL_TIME, localStats.getTotalTime());

        //speed part
        if (SpeedUtils.isSpeedValid(localStats.getAvgSpeed())) {
            contentValues.put(TrackMeContract.RouteEntry.AVG_SPEED, localStats.getAvgSpeed());
        }

        if (SpeedUtils.isSpeedValid(localStats.getMinSpeed())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_SPEED, localStats.getMinSpeed());
        }
        if (SpeedUtils.isSpeedValid(localStats.getMaxSpeed())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_SPEED, localStats.getMaxSpeed());
        }

        if (LocationUtils.isValidLongitude(localStats.getLongitudeMin())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_LONGITUDE, LocationUtils.getLongitude1E6FromDouble(localStats.getLongitudeMin()));
        }
        if (LocationUtils.isValidLongitude(localStats.getLongitudeMax())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_LONGITUDE, LocationUtils.getLongitude1E6FromDouble(localStats.getLongitudeMax()));
        }

        if (LocationUtils.isValidLatitude(localStats.getLatitudeMin())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_LATITUDE, LocationUtils.getLatitude1E6FromDouble(localStats.getLatitudeMin()));
        }
        if (LocationUtils.isValidLatitude(localStats.getLatitudeMax())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_LATITUDE, LocationUtils.getLatitude1E6FromDouble(localStats.getLatitudeMax()));
        }

        if (LocationUtils.isValidElevation(localStats.getElevationMax())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_ELEVATION, localStats.getElevationMin());
        }

        if (LocationUtils.isValidElevation(localStats.getElevationMin())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_ELEVATION, localStats.getElevationMax());
        }
        if (LocationUtils.isValidElevation(localStats.getTotalElevationGain())) {
            contentValues.put(TrackMeContract.RouteEntry.ELEVATION_GAIN, localStats.getTotalElevationGain());
        }

        contentValues.put(TrackMeContract.RouteEntry.NAME, route.getRouteName());

        contentValues.put(TrackMeContract.RouteEntry.NUM_ROUTE_POINTS, route.getNumberOfPoints());
        contentValues.put(TrackMeContract.RouteEntry.TOTAL_DISTANCE, localStats.getTotalDistance());
        contentValues.put(TrackMeContract.RouteEntry.TOTAL_TIME, localStats.getTotalTime());
        contentValues.put(TrackMeContract.RouteEntry.START_POINT_ID, route.getStartPointId());
        contentValues.put(TrackMeContract.RouteEntry.STOP_POINT_ID, route.getStopPointId());
        contentValues.put(TrackMeContract.RouteEntry.START_TIME, localStats.getStartTime());
        contentValues.put(TrackMeContract.RouteEntry.STOP_TIME, localStats.getStopTime());

        return contentValues;
    }

    public static RouteStats getRandomStats() {
        RouteStats routeStats = new RouteStats(now);
        routeStats.setAvgSpeed(DEFAULT_AVG_SPEED);
        routeStats.setStopTime(stop);
        routeStats.setStartTime(start);
        return routeStats;
    }

    public static ContentValues getRoutePointContentValues(RoutePoint routePoint) {
        ContentValues contentValues = new ContentValues();
        if (routePoint.getId() != -1) {
            contentValues.put(TrackMeContract.RoutePointEntry._ID, routePoint.getId());
        }
        if (SpeedUtils.isSpeedValid(routePoint.getSpeed())) {
            contentValues.put(TrackMeContract.RoutePointEntry.SPEED, routePoint.getSpeed());
        }
        if (LocationUtils.isValidLatitude(routePoint.getLatitude())) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LAT, routePoint.getLatitude());
        }

        if (LocationUtils.isValidLongitude(routePoint.getLongitude())) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LAT, routePoint.getLatitude());
        }

        if (LocationUtils.isValidAltitude(routePoint.getAltitude())) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LAT, routePoint.getAltitude());
        }

        return contentValues;
    }
}
