package com.ant.lib.db;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import java.io.File;

/**
 * Data contract for the track me application.
 */
public class TrackMeContract {

    public static final String CONTENT_STRING = "content://";

    public static final String CONTENT_AUTHORITY = "com.ant.track";

    public static final Uri BASE_CONTENT_URI = Uri.parse(CONTENT_STRING + CONTENT_AUTHORITY);

    public static final String PATH_ROUTE = "route";

    public static final String PATH_ROUTE_POINT = "route_point";

    public static final String PATH_ROUTE_MAP_POINT = "route_map_point";


    public static final class RouteEntry implements BaseColumns {
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + File.pathSeparator + CONTENT_AUTHORITY + File.pathSeparator + PATH_ROUTE;

        public static final String TABLE_NAME = "route";

        public static final String NAME = "name";

        public static final String DESCRIPTION = "description";

        public static final String START_POINT_ID = "start_point_id";

        public static final String STOP_POINT_ID = "stop_point_id";

        public static final String START_TIME = "start_time";

        public static final String STOP_TIME = "stop_time";

        public static final String TOTAL_TIME = "total_time";

        public static final String TOTAL_DISTANCE = "total_distance";

        public static final String MIN_LONGITUDE = "min_long";

        public static final String MAX_LONGITUDE = "max_long";

        public static final String NUM_ROUTE_POINTS = "num_route_points";

        public static final String AVG_SPEED = "avg_speed";

        //speed details
        public static final String MAX_SPEED = "max_speed";

        public static final String MAX_ELEVATION = "max_elevation";

        public static final String MIN_ELEVATION = "min_elevation";

    }

    public static final class RoutePointEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE_POINT).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + File.separator + CONTENT_AUTHORITY + File.separator + PATH_ROUTE_POINT;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + File.separator + CONTENT_AUTHORITY + File.separator + PATH_ROUTE_POINT;

        public static final String TABLE_NAME = "route_point";

        public static final String LOCATION_LAT = "location_lat";

        public static final String ROUTE_ID = "route_id";

        public static final String NAME = "name";

        public static final String DESCRIPTION = "description";

        public static final String LOCATION_ACCURACY = "location_accuracy";

        public static final String ROUTE_ACTIVITY_MODE = "route_activity_mode";

        public static final String LOCATION_LONG = "location_long";

        public static final String LOCATION_ALT = "location_alt";

        public static final String TIME = "time";

        public static final String LOCATION_BEARING = "location_bearing";

        public static final String SPEED = "speed";


    }

    public static final class RouteMapPointEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_ROUTE_POINT).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + File.separator + CONTENT_AUTHORITY + File.separator + PATH_ROUTE_POINT;

        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + File.separator + CONTENT_AUTHORITY + File.separator + PATH_ROUTE_POINT;

        public static final String TABLE_NAME = "route_point";

        public static final String LOCATION_LAT = "location_lat";

        public static final String ROUTE_ID = "route_id";

        public static final String NAME = "name";

        public static final String DESCRIPTION = "description";

        public static final String LOCATION_ACCURACY = "location_accuracy";

        public static final String ROUTE_ACTIVITY_MODE = "route_activity_mode";

        public static final String LOCATION_LONG = "location_long";

        public static final String LOCATION_ALT = "location_alt";

        public static final String TIME = "time";

        public static final String DURATION = "duration";

        public static final String TOTAL_TIME = "total_time";

        public static final String LOCATION_BEARING = "location_bearing";

        public static final String SPEED = "speed";

    }
}
