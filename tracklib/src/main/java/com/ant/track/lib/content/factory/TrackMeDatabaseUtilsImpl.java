package com.ant.track.lib.content.factory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.db.DatabaseConstants;
import com.ant.track.lib.db.provider.TrackMeContract;
import com.ant.track.lib.model.Route;
import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.model.RoutePoint;
import com.ant.track.lib.stats.RouteStats;
import com.ant.track.lib.utils.LocationUtils;
import com.ant.track.lib.utils.SpeedUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Track me database utility class. It deals with insertion, deletion,
 * updating and deleting of data from db. It is used for assembling data
 * from db rows and vice-versa. The code is based on the MyTracks app.
 * Some things have been changed some are the same.
 */
public class TrackMeDatabaseUtilsImpl implements TrackMeDatabaseUtils {

    public static final String PARAM_SELECT_MAX = "=(select max(";
    public static final String PARAM_FROM = ") from ";
    public static final String PARAM_WHERE = " WHERE ";
    public static final String PARAM_AND = "=? AND ";
    public static final String PARAM_LESS_OR_EQUAL = "<=";
    public static final String PARAM_COMMA_CLOSE = ")";
    public static final String PARAM_SELECT_MIN = "=(select min(";
    public static final String PARAM_EQUAL_QUESTION_MARK = "=?)";
    public static final String PARAM_EQUAL_STRING = "=?";
    public static final String QUERY_AND = " AND ";
    public static final String QUERY_DESCENDING = " DESCENDING";
    private static final String QUERY_ASCENDING = " ASC";
    private static final String PARAM_LIMIT = " LIMIT";
    private static TrackMeDatabaseUtils instance;
    private int defaultCursorBatchSize = 2000;

    private static final String TAG = TrackMeDatabaseUtilsImpl.class.getCanonicalName();
    private String PARAM_BIG_OR_EQUAL = ">=";

    public static TrackMeDatabaseUtils getInstance() {
        if (instance == null) {
            instance = new TrackMeDatabaseUtilsImpl();
        }
        return instance;
    }


    //===========================================start of public methods===========================================//

    @Override
    public Cursor getRouteCursor(Long routeId) {
        return getRouteCursor(routeId, null);
    }

    @Override
    public Route getRouteById(long routeId) {

        if (routeId < 0) {
            return null;
        }
        Route route = null;
        Cursor cursor = null;
        try {
            cursor = getRouteCursor(routeId, null);
            if (cursor != null && cursor.moveToFirst()) {
                route = createRouteFromCursorInternal(cursor);
            }
        } catch (Exception exc) {
            route = null;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return route;
    }

    @Override
    public int getInsertedPoints(long routeid) {
        if (routeid < 0) {
            return -1;
        }

        Cursor cursor = null;
        try {
            cursor = getRouteCursor(routeid);
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


    @Override
    public List<Route> getAllRoutes() {
        ArrayList<Route> routeList = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = getRouteCursor(null);
            if (cursor != null && cursor.moveToFirst()) {
                routeList.ensureCapacity(cursor.getCount());
                do {
                    routeList.add(createRouteFromCursorInternal(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return routeList;
    }

    @Override
    public List<RoutePoint> getAllRoutePointIds(long routeId) {
        return getAllRoutePointIdsInternal(routeId, null);
    }

    @Override
    public List<RoutePoint> getAllRoutePointIds(long routeId, boolean descending) {
        return getAllRoutePointIds(routeId, descending);
    }

    @Override
    public Uri insertRoutePoint(long routeId, Location location) {
        RoutePoint routePoint = new RoutePoint();
        routePoint.setLocation(location);
        routePoint.setRouteId(routeId);
        return insertRoutePoint(routePoint);
    }

    @Override
    public long getFirstRoutePointId(long routeId) {
        if (routeId < 0) {
            return -1L;
        }
        Cursor cursor = null;
        try {
            String selection = TrackMeContract.RoutePointEntry._ID + PARAM_SELECT_MIN + TrackMeContract.RoutePointEntry._ID
                    + PARAM_FROM + TrackMeContract.RoutePointEntry.TABLE_NAME + PARAM_WHERE + TrackMeContract.RoutePointEntry.ROUTE_ID
                    + PARAM_EQUAL_QUESTION_MARK;
            String[] selectionArgs = new String[]{Long.toString(routeId)};
            cursor = getRoutePointCursorById(new String[]{TrackMeContract.RoutePointEntry._ID},
                    selection,
                    selectionArgs,
                    TrackMeContract.RoutePointEntry._ID);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1L;
    }

    @Override
    public long getLastValidPointId(long routeId) {
        if (routeId < 0) {
            return -1L;
        }
        Cursor cursor = null;
        try {
            String selection = TrackMeContract.RoutePointEntry._ID + PARAM_SELECT_MAX + TrackMeContract.RoutePointEntry._ID
                    + TrackMeContract.RoutePointEntry.TABLE_NAME + TrackMeContract.RoutePointEntry.TABLE_NAME + PARAM_WHERE + TrackMeContract.RoutePointEntry.ROUTE_ID
                    + PARAM_EQUAL_QUESTION_MARK;
            String[] selectionArgs = new String[]{Long.toString(routeId)};
            cursor = getRoutePointCursorById(new String[]{TrackMeContract.RoutePointEntry._ID},
                    selection,
                    selectionArgs,
                    TrackMeContract.RoutePointEntry._ID);
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry._ID));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return -1L;
    }

    @Override
    public RoutePoint getLastRoutePoint(long routeId) {
        if (routeId < 0) {
            return null;
        }
        Cursor cursor;

        try {
            String selection = TrackMeContract.RoutePointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
            String[] args = new String[]{String.valueOf(routeId)};

            cursor = getContentResolver().query(TrackMeContract.RoutePointEntry.CONTENT_URI, null, selection, args, null);
            return createRoutePointFromCursor(cursor);
        } catch (Exception exc) {
            Log.e(TAG, "exception:" + exc);
        }
        return null;

    }


    @Override
    public RouteCheckPoint getLastRouteCheckPoint(long routeId) {
        if (routeId < 0) {
            return null;
        }
        Cursor cursor;

        try {
            String selection = TrackMeContract.RouteCheckPointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
            String[] args = new String[]{String.valueOf(routeId)};
            String sortOrder = DatabaseConstants.DEFAULT_ORDER_COLUMN;
            sortOrder += QUERY_DESCENDING;
            cursor = getContentResolver().query(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, null, selection, args, sortOrder);
            return createRouteCheckPointFromCursorInternal(cursor);
        } catch (Exception exc) {
            Log.e(TAG, "exception:" + exc);
        }
        return null;

    }


    @Override
    public Location getFirstValidRoutePointForRoute(long routeid) {
        if (routeid < 0) {
            return null;
        }
        String selection = TrackMeContract.RoutePointEntry._ID + PARAM_SELECT_MIN + TrackMeContract.RoutePointEntry._ID + PARAM_FROM
                + TrackMeContract.RoutePointEntry.TABLE_NAME + PARAM_WHERE + TrackMeContract.RoutePointEntry.ROUTE_ID + PARAM_AND
                + TrackMeContract.RoutePointEntry.LOCATION_LAT + PARAM_LESS_OR_EQUAL + Constants.MAX_LATITUDE + PARAM_COMMA_CLOSE;
        String[] selectionArgs = new String[]{Long.toString(routeid)};
        return findRoutePointBy(selection, selectionArgs);
    }

    @Override
    public Location getLastValidLocationForRoute(long routeId) {
        if (routeId < 0) {
            return null;
        }
        String selection = TrackMeContract.RoutePointEntry._ID + PARAM_SELECT_MAX + TrackMeContract.RoutePointEntry._ID + PARAM_FROM
                + TrackMeContract.RoutePointEntry.TABLE_NAME + PARAM_WHERE + TrackMeContract.RoutePointEntry.ROUTE_ID + PARAM_AND
                + TrackMeContract.RoutePointEntry.LOCATION_LAT + PARAM_LESS_OR_EQUAL + Constants.MAX_LATITUDE + PARAM_COMMA_CLOSE;
        String[] selectionArgs = new String[]{Long.toString(routeId)};
        return findRoutePointBy(selection, selectionArgs);
    }

    @Override
    public Uri insertRouteTrack(Route route) {
        return getApp().getContentResolver().insert(TrackMeContract.RouteEntry.CONTENT_URI, createContentValues(route));
    }

    @Override
    public void deleteRouteTrack(long id) {
        deleteAllRoutePoints(id);
        String where = getWhereCloseForRoute();
        String[] selectionArgs = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RouteEntry.CONTENT_URI, where, selectionArgs);
    }

    @Override
    public int updateRouteTrack(Route route) {
        String where = TrackMeContract.RouteEntry._ID + PARAM_EQUAL_STRING;
        String[] args = new String[]{String.valueOf(route.getRouteId())};
        ContentValues values = createContentValues(route);
        return getContentResolver().update(TrackMeContract.RouteEntry.CONTENT_URI, values, where, args);
    }

    @Override
    public int updateRouteTrack(long id, ContentValues values) {
        String where = TrackMeContract.RouteEntry._ID + PARAM_EQUAL_STRING;
        String[] args = new String[]{String.valueOf(id)};
        return getContentResolver().update(TrackMeContract.RouteEntry.CONTENT_URI, values, where, args);
    }

    @Override
    public Uri insertRoutePoint(RoutePoint routePoint) {
        return getApp().getContentResolver().insert(TrackMeContract.RoutePointEntry.CONTENT_URI,
                createContentValues(routePoint));
    }

    @Override
    public Uri insertRouteCheckPoint(RouteCheckPoint routeCheckPoint) {
        return getApp().getContentResolver().insert(TrackMeContract.RoutePointEntry.CONTENT_URI,
                createContentValues(routeCheckPoint));
    }

    @Override
    public void deleteRoutePoint(long id) {
        String where = getWhereCloseForRoutePoint();
        String[] args = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RoutePointEntry.CONTENT_URI, where, args);
    }


    @Override
    public void deleteRouteCheckPoint(long id) {
        String where = getWhereCloseForRouteCheckPoint();
        String[] args = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, where, args);
    }

    @Override
    public int updateRoutePointById(long id, ContentValues values) {
        String where = getWhereCloseForRouteCheckPoint();
        String[] args = new String[]{String.valueOf(id)};
        return getContentResolver().update(TrackMeContract.RoutePointEntry.CONTENT_URI, values, where, args);
    }

    @Override
    public int updateRoutePoint(RoutePoint routePoint) {
        String where = TrackMeContract.RouteEntry._ID + PARAM_EQUAL_STRING;
        String[] args = new String[]{String.valueOf(routePoint.getId())};
        ContentValues values = createContentValues(routePoint);
        return getContentResolver().update(TrackMeContract.RouteEntry.CONTENT_URI, values, where, args);
    }


    @Override
    public LocationIterator getRoutePointsIterator(final long routeId,
                                                   final long startRoutePointId) {
        return new LocationIterator() {
            private boolean descending = true;
            private long lastRoutePointId = -1L;
            private Cursor cursor = getCursor(startRoutePointId);
            private List<RoutePoint> routePoints = getAllRoutePointsByRouteId(routeId);
            private final CachedTrackPointsIndexes indexes = cursor != null ? new CachedTrackPointsIndexes(cursor) : null;

            /**
             * Gets the routepoint cursor.
             *
             * @param routePointId the starting route point id
             */
            private Cursor getCursor(long routePointId) {
                return getRoutePointCursorById(routeId, routePointId, defaultCursorBatchSize, descending);
            }


            private List<RoutePoint> getAllRoutePointsByRouteId(long routeid) {
                return getAllRoutePointIds(routeid, descending);
            }

            /**
             * Advances the cursor to the next batch. Returns true if successful.
             */
            private boolean advanceCursorToNextBatch() {
                close();
                long lastRoutePointId = -1L;
                int lastIndex = routePoints.size() - 1;
                if (lastIndex >= 0) {
                    RoutePoint routePoint = routePoints.remove(lastIndex);
                    lastRoutePointId = routePoint.getId();
                }
                cursor = getCursor(lastRoutePointId);
                return cursor != null;
            }

            @Override
            public long getLocationId() {
                return lastRoutePointId;
            }

            @Override
            public boolean hasNext() {

                if (cursor == null) {
                    return false;
                }

                if (cursor.isAfterLast()) {
                    return false;
                }

                if (cursor.isLast()) {
                    if (cursor.getCount() != defaultCursorBatchSize) {
                        return false;
                    }
                    return advanceCursorToNextBatch();
                }

                return true;
            }

            @Override
            public Location next() {

                if (!cursor.moveToNext() || !advanceCursorToNextBatch()) {
                    throw new NoSuchElementException("no element found");
                }

                Location loc = LocationUtils.createLocation();
                lastRoutePointId = cursor.getLong(indexes.idIndex);
                return updateRoutePointFromCursorAndCache(cursor, indexes, loc);
            }

            @Override
            public void close() {
                if (cursor != null) {
                    cursor.close();
                }
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Cursor getNewRouteCheckPointsCursor(long routeid, long startId, int totalNumberOfPoints) {

        if (routeid < 0) {
            return null;
        }
        String[] args;
        String where;
        try {

            if (startId >= 0) {

                where = TrackMeContract.RouteCheckPointEntry.ROUTE_ID + PARAM_EQUAL_STRING
                        + PARAM_AND
                        + TrackMeContract.RouteCheckPointEntry._ID + PARAM_BIG_OR_EQUAL + PARAM_EQUAL_STRING;
                args = new String[]{Long.toString(routeid), Long.toString(startId)};

            } else {
                where = TrackMeContract.RouteCheckPointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
                args = new String[]{Long.toString(routeid)};
            }

            String sortOrder = TrackMeContract.RouteCheckPointEntry._ID;
            if (totalNumberOfPoints >= 0) {
                sortOrder += PARAM_LIMIT + totalNumberOfPoints;
            }

            return getContentResolver().query(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, null, where, args, sortOrder);

        } catch (Exception ex) {
            Log.e(TAG, "exception in returning the cursor" + ex.getMessage());
            return null;
        }

    }


    @Override
    public Route createRouteFromCursor(Cursor cursor) {
        return createRouteFromCursorInternal(cursor);
    }

    @Override
    public RoutePoint createRoutePointFromCursor(Cursor cursor) {
        return createRoutePointFromCursorInternal(cursor);
    }


    //====================================end of public methods==================================================//

    //====================================private methods========================================================//


    @NonNull
    private List<RoutePoint> getAllRoutePointIdsInternal(long routeId, Boolean descending) {
        List<RoutePoint> routeList = new LinkedList<>();
        Cursor cursor = null;
        try {
            cursor = getRoutePointCursorById(routeId, descending);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    routeList.add(createRoutePointFromCursor(cursor));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return routeList;
    }

    private Cursor getRoutePointCursorById(long routeId, Boolean descending) {
        String where = null;
        String[] args = null;
        String sortOrder = null;
        if (routeId != -1L) {
            where = TrackMeContract.RoutePointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
            args = new String[]{Long.toString(routeId)};
            sortOrder = DatabaseConstants.DEFAULT_ORDER_COLUMN;
            if (descending) {
                sortOrder += QUERY_DESCENDING;
            } else {
                sortOrder += QUERY_ASCENDING;
            }
        }
        return getContentResolver().query(TrackMeContract.RoutePointEntry.CONTENT_URI, null, where, args, sortOrder);
    }

    private Cursor getRoutePointCursorById(long routeId) {
        String where = null;
        String[] args = null;
        if (routeId != -1L) {
            where = TrackMeContract.RoutePointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
            args = new String[]{Long.toString(routeId)};
        }
        return getContentResolver().query(TrackMeContract.RoutePointEntry.CONTENT_URI, null, where, args, null);
    }

    private Cursor getRoutePointCursorById() {
        return getContentResolver().query(TrackMeContract.RoutePointEntry.CONTENT_URI, null, null, null, DatabaseConstants.DEFAULT_ORDER_COLUMN);
    }


    private Cursor getRoutePointCursorById(String[] strings, String selection, String[] selectionArgs, String sortOrder) {
        return getContentResolver().query(TrackMeContract.RoutePointEntry.CONTENT_URI, strings, selection, selectionArgs, sortOrder);
    }

    private ContentValues createContentValues(RoutePoint routePoint) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TrackMeContract.RoutePointEntry.ROUTE_ID, routePoint.getRouteId());
        if (LocationUtils.isValidLatitude(routePoint.getLatitude())) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LAT, LocationUtils.getLatitude1E6FromDouble(routePoint.getLatitude()));
        }
        if (LocationUtils.isValidLongitude(routePoint.getLongitude())) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_LONG, LocationUtils.getLongitude1E6FromDouble(routePoint.getLongitude()));
        }

        long time;
        if (routePoint.getTime() == 0) {
            time = System.currentTimeMillis();
        } else {
            time = routePoint.getTime();
        }
        contentValues.put(TrackMeContract.RoutePointEntry.TIME, time);

        if (routePoint.hasSpeed()) {
            contentValues.put(TrackMeContract.RoutePointEntry.SPEED, routePoint.getSpeed());
        }

        if (routePoint.hasAccuracy()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_ACCURACY, routePoint.getAccuracy());
        }

        if (routePoint.hasAltitude()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_ALT, routePoint.getAltitude());
        }

        if (routePoint.hasBearing()) {
            contentValues.put(TrackMeContract.RoutePointEntry.LOCATION_BEARING, routePoint.getBearing());
        }

        return contentValues;
    }


    private ContentValues createContentValues(RouteCheckPoint routeCheckPoint) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(TrackMeContract.RouteCheckPointEntry.NAME, routeCheckPoint.getName());
        contentValues.put(TrackMeContract.RouteCheckPointEntry.DESCRIPTION, routeCheckPoint.getDescription());
        contentValues.put(TrackMeContract.RouteCheckPointEntry.MARKER_COLOR, routeCheckPoint.getMarkerColor());
        contentValues.put(TrackMeContract.RoutePointEntry.ROUTE_ID, routeCheckPoint.getId());
        if (LocationUtils.isValidLatitude(routeCheckPoint.getLatitude())) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_LAT, LocationUtils.getLatitude1E6FromDouble(routeCheckPoint.getLatitude()));
        }
        if (LocationUtils.isValidLongitude(routeCheckPoint.getLongitude())) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_LONG, LocationUtils.getLongitude1E6FromDouble(routeCheckPoint.getLongitude()));
        }

        long time;
        if (routeCheckPoint.getTime() == 0) {
            time = System.currentTimeMillis();
        } else {
            time = routeCheckPoint.getTime();
        }
        contentValues.put(TrackMeContract.RouteCheckPointEntry.TIME, time);

        if (routeCheckPoint.hasSpeed()) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.SPEED, routeCheckPoint.getSpeed());
        }

        if (routeCheckPoint.hasAccuracy()) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_ACCURACY, routeCheckPoint.getAccuracy());
        }

        if (routeCheckPoint.hasAltitude()) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_ALT, routeCheckPoint.getAltitude());
        }

        if (routeCheckPoint.hasBearing()) {
            contentValues.put(TrackMeContract.RouteCheckPointEntry.LOCATION_BEARING, routeCheckPoint.getBearing());
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
        if (SpeedUtils.isSpeedValid(routeStats.getAvgSpeed())) {
            contentValues.put(TrackMeContract.RouteEntry.AVG_SPEED, routeStats.getAvgSpeed());
        }
        if (SpeedUtils.isSpeedValid(routeStats.getMinSpeed())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_SPEED, routeStats.getMinSpeed());
        }
        if (SpeedUtils.isSpeedValid(routeStats.getMaxSpeed())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_SPEED, routeStats.getMaxSpeed());
        }
        if (LocationUtils.isValidLongitude(routeStats.getLongitudeMin())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_LONGITUDE, LocationUtils.getLongitude1E6FromDouble(routeStats.getLongitudeMin()));
        }
        if (LocationUtils.isValidLongitude(routeStats.getLongitudeMax())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_LONGITUDE, LocationUtils.getLongitude1E6FromDouble(routeStats.getLongitudeMax()));
        }

        if (LocationUtils.isValidLatitude(routeStats.getLatitudeMin())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_LATITUDE, LocationUtils.getLatitude1E6FromDouble(routeStats.getLatitudeMin()));
        }

        if (LocationUtils.isValidLatitude(routeStats.getLatitudeMax())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_LATITUDE, LocationUtils.getLatitude1E6FromDouble(routeStats.getLatitudeMax()));
        }

        if (LocationUtils.isValidElevation(routeStats.getElevationMin())) {
            contentValues.put(TrackMeContract.RouteEntry.MIN_ELEVATION, LocationUtils.getElevation1E6FromDouble(routeStats.getElevationMin()));
        }
        if (LocationUtils.isValidElevation(routeStats.getElevationMax())) {
            contentValues.put(TrackMeContract.RouteEntry.MAX_ELEVATION, LocationUtils.getElevation1E6FromDouble(routeStats.getElevationMax()));
        }
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

    private void deleteAllRoutePoints(long id) {
        //delete route points first
        String where = TrackMeContract.RoutePointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
        String[] selectArgs = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RoutePointEntry.CONTENT_URI, where, selectArgs);
        //route check point
        where = TrackMeContract.RouteCheckPointEntry.ROUTE_ID + PARAM_EQUAL_STRING;
        selectArgs = new String[]{String.valueOf(id)};
        getContentResolver().delete(TrackMeContract.RouteCheckPointEntry.CONTENT_URI, where, selectArgs);

    }

    private Location findRoutePointBy(String selection, String[] selectionArgs) {
        Location location = null;
        Cursor cursor = null;
        try {
            cursor = getContentResolver().query(TrackMeContract.RoutePointEntry.CONTENT_URI, null, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToNext()) {
                location = getLocationFromCursor(cursor);
            }
        } catch (Exception exc) {
            Log.e(TAG, "exception in retrieving data.");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return location;
    }

    private Location getLocationFromCursor(Cursor cursor) {
        Location location = LocationUtils.createLocation();
        CachedTrackPointsIndexes cursorIndexes = new CachedTrackPointsIndexes(cursor);
        if (!cursor.isNull(cursorIndexes.latitudeIndex)) {
            location.setLatitude(((double) cursor.getInt(cursorIndexes.latitudeIndex)) / 1E6);
        }

        if (!cursor.isNull(cursorIndexes.longitudeIndex)) {
            location.setLongitude(((double) cursor.getInt(cursorIndexes.longitudeIndex)) / 1E6);
        }

        if (!cursor.isNull(cursorIndexes.altitudeIndex)) {
            location.setAltitude(cursor.getFloat(cursorIndexes.altitudeIndex));
        }

        if (!cursor.isNull(cursorIndexes.accuracyIndex)) {
            location.setAccuracy(cursor.getFloat(cursorIndexes.accuracyIndex));
        }

        if (!cursor.isNull(cursorIndexes.bearingIndex)) {
            location.setBearing(cursor.getFloat(cursorIndexes.bearingIndex));
        }

        return location;
    }


    private Cursor getRoutePointCursorById(long routeId, long startRoutePointId, int defaultCursorBatchSize, boolean descending) {
        if (routeId == -1L) {
            return null;
        }

        String selection = "";
        String[] selectionArgs;

        if (startRoutePointId >= 0) {
            String comparison = descending ? "<=" : ">=";
            selection += TrackMeContract.RoutePointEntry._ID + PARAM_EQUAL_STRING + QUERY_AND + TrackMeContract.RoutePointEntry.ROUTE_ID + comparison;
            selectionArgs = new String[]{String.valueOf(startRoutePointId), String.valueOf(routeId)};

        } else {
            selection = TrackMeContract.RoutePointEntry._ID;
            selectionArgs = new String[]{String.valueOf(routeId)};
        }

        String sortOrderQuery = TrackMeContract.RoutePointEntry._ID;
        if (descending) {
            sortOrderQuery += QUERY_DESCENDING;
        }

        if (defaultCursorBatchSize > 0) {
            sortOrderQuery += " LIMIT " + defaultCursorBatchSize;
        }
        return getRoutePointCursorById(null, selection, selectionArgs, sortOrderQuery);
    }

    /**
     * A cache of track points indexes.
     */
    private static class CachedTrackPointsIndexes {
        public final int idIndex;
        public final int longitudeIndex;
        public final int latitudeIndex;
        public final int timeIndex;
        public final int altitudeIndex;
        public final int accuracyIndex;
        public final int speedIndex;
        public final int bearingIndex;

        public CachedTrackPointsIndexes(Cursor cursor) {
            idIndex = cursor.getColumnIndex(TrackMeContract.RoutePointEntry._ID);
            longitudeIndex = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_LONG);
            latitudeIndex = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_LAT);
            timeIndex = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.TIME);
            altitudeIndex = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_ALT);
            accuracyIndex = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_ACCURACY);
            speedIndex = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.SPEED);
            bearingIndex = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_BEARING);
        }

    }

    @Override
    public RouteCheckPoint getRouteCheckPointFromCursor(Cursor cursor) {
        return createRouteCheckPointFromCursorInternal(cursor);
    }

    /**
     * Fills a track point from a cursor.
     *
     * @param cursor           the cursor pointing to a location.
     * @param indexes          the cached track points indexes
     * @param locationToUpdate the track point
     */
    private Location updateRoutePointFromCursorAndCache(Cursor cursor, CachedTrackPointsIndexes indexes, final Location locationToUpdate) {

        Location location = locationToUpdate;
        location.reset();

        if (!cursor.isNull(indexes.longitudeIndex)) {
            location.setLongitude(((double) cursor.getInt(indexes.longitudeIndex)) / 1E6);
        }
        if (!cursor.isNull(indexes.latitudeIndex)) {
            location.setLatitude(((double) cursor.getInt(indexes.latitudeIndex)) / 1E6);
        }
        if (!cursor.isNull(indexes.timeIndex)) {
            location.setTime(cursor.getLong(indexes.timeIndex));
        }
        if (!cursor.isNull(indexes.altitudeIndex)) {
            location.setAltitude(cursor.getFloat(indexes.altitudeIndex));
        }
        if (!cursor.isNull(indexes.accuracyIndex)) {
            location.setAccuracy(cursor.getFloat(indexes.accuracyIndex));
        }
        if (!cursor.isNull(indexes.speedIndex)) {
            location.setSpeed(cursor.getFloat(indexes.speedIndex));
        }
        if (!cursor.isNull(indexes.bearingIndex)) {
            location.setBearing(cursor.getFloat(indexes.bearingIndex));
        }
        return location;
    }

    private synchronized RouteCheckPoint createRouteCheckPointFromCursorInternal(Cursor cursor) {
        int idxId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry._ID);
        int idxName = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.NAME);
        int idxDescription = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.DESCRIPTION);
        int idxRouteId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.ROUTE_ID);
        int idxSpeed = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.SPEED);
        int idxTime = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.TIME);

        int idxLocAccuracy = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_ACCURACY);
        int idxLocLatitude = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_LAT);
        int idxLocLongitude = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_LONG);
        int idxLocBearing = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_BEARING);
        int idxLocAlt = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.LOCATION_ALT);
        int idxMarkerColor = cursor.getColumnIndexOrThrow(TrackMeContract.RouteCheckPointEntry.MARKER_COLOR);

        RouteCheckPoint routeCheckPoint = new RouteCheckPoint();
        if (!isNull(cursor, idxId)) {
            routeCheckPoint.setId(cursor.getLong(idxId));
        }
        //name
        if (!isNull(cursor, idxName)) {
            routeCheckPoint.setName(cursor.getString(idxName));
        }
        //desc
        if (!isNull(cursor, idxDescription)) {
            routeCheckPoint.setDescription(cursor.getString(idxDescription));
        }
        //stats
        if (!isNull(cursor, idxSpeed)) {
            routeCheckPoint.setSpeed(cursor.getFloat(idxSpeed));
        }
        if (!isNull(cursor, idxTime)) {
            routeCheckPoint.setTime(cursor.getLong(idxTime));
        }

        if (!isNull(cursor, idxLocAccuracy)) {
            routeCheckPoint.setLocationBearing(cursor.getFloat(idxLocBearing));
        }

        if (!isNull(cursor, idxLocAlt)) {
            routeCheckPoint.setLocation_alt(cursor.getFloat(idxLocAlt));
        }

        if (!isNull(cursor, idxLocLatitude) &&
                !isNull(cursor, idxLocLongitude)) {
            routeCheckPoint.setLocation_lat(LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocLatitude)));
            routeCheckPoint.setLocation_long(LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocLongitude)));
        }

        if (!isNull(cursor, idxMarkerColor)) {
            routeCheckPoint.setLocationBearing(cursor.getInt(idxMarkerColor));
        }

        if (!isNull(cursor, idxRouteId)) {
            routeCheckPoint.setRouteId(cursor.getLong(idxLocBearing));
        }
        return routeCheckPoint;

    }

    private synchronized RoutePoint createRoutePointFromCursorInternal(Cursor cursor) {
        int idxId = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry._ID);
        int idxRouteId = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.ROUTE_ID);
        int idxSpeed = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.SPEED);
        int idxTime = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.TIME);

        int idxLocAccuracy = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_ACCURACY);
        int idxLocLatitude = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_LAT);
        int idxLocLongitude = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_LONG);
        int idxLocBearing = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_BEARING);
        int idxLocAlt = cursor.getColumnIndexOrThrow(TrackMeContract.RoutePointEntry.LOCATION_ALT);

        RoutePoint routePoint = new RoutePoint();
        if (!isNull(cursor, idxId)) {
            routePoint.setId(cursor.getLong(idxId));
        }

        //stats
        if (!isNull(cursor, idxSpeed)) {
            routePoint.setSpeed(cursor.getFloat(idxSpeed));
        }
        if (!isNull(cursor, idxTime)) {
            routePoint.setTime(cursor.getLong(idxTime));
        }

        if (!isNull(cursor, idxLocAccuracy)) {
            routePoint.setLocationBearing(cursor.getFloat(idxLocBearing));
        }

        if (!isNull(cursor, idxLocAlt)) {
            routePoint.setLocation_alt(cursor.getFloat(idxLocAlt));
        }

        if (!isNull(cursor, idxLocLatitude) &&
                !isNull(cursor, idxLocLongitude)) {
            routePoint.setLocation_lat(LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocLatitude)));
            routePoint.setLocation_long(LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocLongitude)));
        }


        if (!isNull(cursor, idxRouteId)) {
            routePoint.setRouteId(cursor.getLong(idxLocBearing));
        }
        return routePoint;

    }


    private synchronized Route createRouteFromCursorInternal(Cursor cursor) {
        int idxId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry._ID);
        int idxName = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.NAME);
        int idxDescription = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.DESCRIPTION);

        int idxAvgSpeed = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.AVG_SPEED);
        int idxMinSpeed = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MIN_SPEED);
        int idxMaxSpeed = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MAX_SPEED);

        int idxLocMinLat = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MIN_LATITUDE);
        int idxLocMaxLat = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MAX_LATITUDE);

        int idxLocMinLong = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MIN_LONGITUDE);
        int idxLocMaxLong = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MAX_LONGITUDE);

        int idxLocMinElev = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MIN_ELEVATION);
        int idxLocMaxElev = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.MAX_ELEVATION);

        int idxNumPoints = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.NUM_ROUTE_POINTS);
        int idxTotalTime = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.TOTAL_TIME);
        int idxTotalMovingTime = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.TOTAL_MOVING_TIME);

        int idxDistance = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.TOTAL_DISTANCE);

        int idxStartPointId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.START_POINT_ID);
        int idxStopPointId = cursor.getColumnIndexOrThrow(TrackMeContract.RouteEntry.STOP_POINT_ID);
        Route route = new Route();
        RouteStats routeStats = new RouteStats();
        if (!isNull(cursor, idxId)) {
            route.setRouteId(cursor.getLong(idxId));
        }
        //name
        if (!isNull(cursor, idxName)) {
            route.setRouteName(cursor.getString(idxName));
        }
        //desc
        if (!isNull(cursor, idxDescription)) {
            route.setDescription(cursor.getString(idxDescription));
        }
        //stats
        if (!isNull(cursor, idxAvgSpeed)) {
            routeStats.setAvgSpeed(cursor.getDouble(idxAvgSpeed));
        }
        if (!isNull(cursor, idxMinSpeed)) {
            routeStats.setMinSpeed(cursor.getDouble(idxMinSpeed));
        }

        if (!isNull(cursor, idxMaxSpeed)) {
            routeStats.setMaxSpeed(cursor.getDouble(idxMaxSpeed));
        }

        if (!isNull(cursor, idxLocMinLat) &&
                !isNull(cursor, idxLocMaxLat) &&
                !isNull(cursor, idxLocMinLong) &&
                !isNull(cursor, idxLocMaxLong)) {
            routeStats.updateMinMaxLatitudeStats(LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocMinLat)), LocationUtils.getLatitudeFromLatitude1E6(cursor.getInt(idxLocMaxLat)));
            routeStats.updateMinMaxLongStats(LocationUtils.getLongitudeFromLongitude1E6(cursor.getInt(idxLocMinLong)), LocationUtils.getLongitudeFromLongitude1E6(cursor.getInt(idxLocMaxLong)));
        }

        if (!isNull(cursor, idxLocMinElev) && !isNull(cursor, idxLocMaxElev)) {
            routeStats.updateMinMaxElevation(LocationUtils.getElevationFromElevation1E6(cursor.getInt(idxLocMinElev)), LocationUtils.getElevationFromElevation1E6(cursor.getInt(idxLocMaxElev)));
        }

        if (!isNull(cursor, idxStartPointId)) {
            route.setStartPointId(cursor.getLong(idxStartPointId));
        }

        if (!isNull(cursor, idxStopPointId)) {
            route.setStopPointId(cursor.getLong(idxStopPointId));
        }

        if (!isNull(cursor, idxTotalTime)) {
            routeStats.setTotalTime(cursor.getInt(idxTotalTime));
        }

        if (!isNull(cursor, idxNumPoints)) {
            route.setNumberOfPoints(cursor.getInt(idxNumPoints));
        }

        if (!isNull(cursor, idxDistance)) {
            routeStats.setTotalDistance(cursor.getDouble(idxDistance));
        }

        if (!isNull(cursor, idxTotalMovingTime)) {
            routeStats.setTotalMovingTime(cursor.getLong(idxTotalMovingTime));
        }
        route.setRouteStats(routeStats);
        return route;

    }

    private boolean isNull(Cursor cursor, int index) {
        return cursor.isNull(index);
    }


    private Cursor getRouteCursor(Long routeId, String[] projection) {
        String where = null;
        String[] selectArgs = null;
        if (routeId != null) {
            where = TrackMeContract.RouteEntry._ID + PARAM_EQUAL_STRING;
            selectArgs = new String[]{String.valueOf(routeId)};
        }
        return getContentResolver().query(TrackMeContract.RouteEntry.CONTENT_URI, projection, where, selectArgs, DatabaseConstants.DEFAULT_ORDER_COLUMN);

    }

    private ContentResolver getContentResolver() {
        return TrackLibApplication.getInstance().getContentResolver();
    }

    @NonNull
    private String getWhereCloseForRoute() {
        return TrackMeContract.RouteEntry._ID + PARAM_EQUAL_STRING;
    }

    @NonNull
    private String getWhereCloseForRoutePoint() {
        return TrackMeContract.RoutePointEntry._ID + PARAM_EQUAL_STRING;
    }

    @NonNull
    private String getWhereCloseForRouteCheckPoint() {
        return TrackMeContract.RouteCheckPointEntry._ID + PARAM_EQUAL_STRING;
    }


    //====================================end of private methods===============================================//

    private TrackLibApplication getApp() {
        return TrackLibApplication.getInstance();
    }

    public static void reset() {
        instance = null;
    }
}
