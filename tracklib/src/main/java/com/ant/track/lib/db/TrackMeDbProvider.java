package com.ant.track.lib.db;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.ant.track.lib.application.TrackLibApplication;
import com.ant.track.lib.db.helper.TrackMeOpenHelper;

/**
 * Data provider for the TrackMe application.
 */
public class TrackMeDbProvider extends ContentProvider {

    private static final int ROUTE = 100;
    private static final int ROUTE_ID = 101;

    private static final int ROUTE_POINT = 200;
    private static final int ROUTE_POINT_ID = 201;

    private static final int ROUTE_CHECK_POINT = 300;
    private static final int ROUTE_CHECK_POINT_ID = 301;

    private static final String PATH_SEPARATOR_PROVIDER = "/#";

    private TrackMeOpenHelper trackMeOpenHelper;

    private static UriMatcher buildUriMatcher() {

        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = TrackMeContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, TrackMeContract.PATH_ROUTE, UriType.ROUTE.ordinal());
        matcher.addURI(authority, TrackMeContract.PATH_ROUTE + PATH_SEPARATOR_PROVIDER, UriType.ROUTE_ID.ordinal());

        matcher.addURI(authority, TrackMeContract.PATH_ROUTE_POINT, UriType.ROUTE_POINT.ordinal());
        matcher.addURI(authority, TrackMeContract.PATH_ROUTE_POINT + PATH_SEPARATOR_PROVIDER, UriType.ROUTE_POINT_ID.ordinal());

        matcher.addURI(authority, TrackMeContract.PATH_ROUTE, UriType.ROUTE.ordinal());
        matcher.addURI(authority, TrackMeContract.PATH_ROUTE + PATH_SEPARATOR_PROVIDER, UriType.ROUTE.ordinal());

        matcher.addURI(authority, TrackMeContract.PATH_ROUTE_CHECK_POINT, UriType.ROUTE_CHECK_POINT.ordinal());
        matcher.addURI(authority, TrackMeContract.PATH_ROUTE_CHECK_POINT + PATH_SEPARATOR_PROVIDER, UriType.ROUTE_CHECK_POINT_ID.ordinal());

        return matcher;
    }

    private static final UriMatcher sUriMatcher = buildUriMatcher();

    @Override
    public boolean onCreate() {
        trackMeOpenHelper = new TrackMeOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            // Student: Uncomment and fill out these two cases
            case ROUTE:
                return TrackMeContract.RouteEntry.CONTENT_TYPE;
            case ROUTE_POINT:
                return TrackMeContract.RoutePointEntry.CONTENT_TYPE;
            case ROUTE_CHECK_POINT:
                return TrackMeContract.RouteCheckPointEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor queryCursor;

        int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE: {
                queryCursor = trackMeOpenHelper.getReadableDatabase().query(TrackMeContract.RouteEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            case ROUTE_POINT: {
                queryCursor = trackMeOpenHelper.getReadableDatabase().query(TrackMeContract.RoutePointEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }
            default:
                throw new UnsupportedOperationException("Query for data failed!");
        }
        queryCursor.setNotificationUri(getContentResolver(), uri);
        return queryCursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = trackMeOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ROUTE: {
                long _id = db.insert(TrackMeContract.RouteEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = TrackMeContract.RouteEntry.buildRouteUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case ROUTE_POINT: {
                long _id = db.insert(TrackMeContract.RoutePointEntry.TABLE_NAME, null, values);
                if (_id > 0) {
                    returnUri = TrackMeContract.RoutePointEntry.buildRoutePointUri(_id);
                } else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        notifyChange(returnUri);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = trackMeOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case ROUTE: {
                rowsDeleted = db.delete(TrackMeContract.RouteEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case ROUTE_POINT: {
                rowsDeleted = db.delete(TrackMeContract.RoutePointEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri when trying to delete");
        }
        if (selection == null || rowsDeleted != 0) {
            notifyChange(uri);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = trackMeOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case ROUTE: {
                rowsDeleted = db.update(TrackMeContract.RouteEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            case ROUTE_POINT: {
                rowsDeleted = db.update(TrackMeContract.RoutePointEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri when trying to delete");
        }
        if (rowsDeleted != 0) {
            notifyChange(uri);
        }
        return rowsDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = trackMeOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE: {
                db.beginTransaction();
                int count = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(TrackMeContract.RouteEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            count++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                notifyChange(uri);
                return count;
            }
            default:
                return super.bulkInsert(uri, values);
        }
    }

    //used for testing more below:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        trackMeOpenHelper.close();
        super.shutdown();
    }

    private void notifyChange(Uri returnUri) {
        getContentResolver().notifyChange(returnUri, null);
    }

    private ContentResolver getContentResolver() {
        return TrackLibApplication.getInstance().getContentResolver();
    }

    enum UriType {
        ROUTE,
        ROUTE_ID,
        ROUTE_POINT,
        ROUTE_POINT_ID,
        ROUTE_CHECK_POINT,
        ROUTE_CHECK_POINT_ID
    }
}
