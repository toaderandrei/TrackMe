package com.ant.track.lib.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.ant.track.lib.db.helper.TrackMeOpenHelper;

/**
 * Data provider for the TrackMe application.
 */
public class TrackMeDbProvider extends ContentProvider {

    private static final int ROUTE = 100;
    private static final int ROUTE_ID = 101;

    private static final int ROUTE_POINT = 200;
    private static final int ROUTE_POINT_ID = 201;

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

        matcher.addURI(authority, TrackMeContract.PATH_ROUTE_MAP_POINT, UriType.ROUTE_MAP_POINT.ordinal());
        matcher.addURI(authority, TrackMeContract.PATH_ROUTE_MAP_POINT + PATH_SEPARATOR_PROVIDER, UriType.ROUTE_MAP_POINT_ID.ordinal());

        return matcher;
    }

    @Override
    public boolean onCreate() {
        trackMeOpenHelper = new TrackMeOpenHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    enum UriType {
        ROUTE,
        ROUTE_ID,
        ROUTE_POINT,
        ROUTE_POINT_ID,
        ROUTE_MAP_POINT,
        ROUTE_MAP_POINT_ID
    }
}
