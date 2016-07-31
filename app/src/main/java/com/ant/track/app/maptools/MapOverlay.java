package com.ant.track.app.maptools;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.ant.track.app.R;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class that deals with storing location points, markers,
 * start points, end points, etc.
 */
public class MapOverlay {


    private boolean showEndMarker = true;

    public static final float WAYPOINT_X_ANCHOR = 13f / 48f;

    private static final float WAYPOINT_Y_ANCHOR = 43f / 48f;
    private static final float MARKER_X_ANCHOR = 50f / 96f;
    private static final float MARKER_Y_ANCHOR = 90f / 96f;
    private static final int INITIAL_LOCATIONS_SIZE = 1024;

    private BlockingQueue<CachedLocation> pendingLocations = null;

    private static final String TAG = MapOverlay.class.getCanonicalName();
    private static final int ROUTE_COLOR = PreferenceUtils.DEFAULT_ROUTE_COLOR;
    private List<CachedLocation> locations = null;
    private List<RouteCheckPoint> checkpoints;
    private final Object lock = new Object();
    private DrawPath drawPath;

    public MapOverlay(Context context) {
        pendingLocations = new ArrayBlockingQueue<>(2 * Constants.DEFAULT_MAX_NUMBER_OF_POINTS);
        locations = new ArrayList<>();
        checkpoints = new ArrayList<>();
        drawPath = new DrawPath(context, ROUTE_COLOR);
    }


    /**
     * Sets whether to show the end marker.
     *
     * @param show true to show the end marker
     */
    public void setShowEndMarker(boolean show) {
        showEndMarker = show;
    }

    public void clearPoints() {
        synchronized (lock) {
            locations.clear();
            checkpoints.clear();
            pendingLocations.clear();
        }
    }

    public void addRouteCheckPoint(RouteCheckPoint routeCheckPoint) {
        synchronized (lock) {
            if (routeCheckPoint != null && !checkpoints.contains(routeCheckPoint)) {
                checkpoints.add(routeCheckPoint);
            }
        }
    }

    public void addLocation(Location location) {
        //todo replace location with cached
        if (location != null) {
            pendingLocations.offer(new CachedLocation(location));
        } else {
            Log.i(TAG, "cannot insert the location or location is null");
        }
    }

    public void addLocationToMap(Location location) {
        if (location != null) {
            pendingLocations.offer(new CachedLocation(location));
        } else {
            Log.i(TAG, "cannot insert the location or location is null");
        }
    }

    public boolean update(GoogleMap mMap, List<Polyline> path, boolean reload) {
        synchronized (lock) {
            boolean hasStartMarker = false;
            int newLocations = pendingLocations.drainTo(locations);

            if (newLocations >= 0 && reload) {
                mMap.clear();
                drawPath.updatePath(mMap, path, 0, locations);
                hasStartMarker = updateStartAndEndMarkers(mMap);
                updateCheckRoutePoints(mMap);
            } else {
                if (newLocations > 0) {
                    int totalLocations = locations.size();
                    drawPath.updatePath(mMap, path, totalLocations - newLocations, locations);
                }
            }
            return hasStartMarker;
        }
    }

    /**
     * Updates the start and end markers.
     *
     * @param googleMap the google map
     * @return true if has the start marker
     */
    private boolean updateStartAndEndMarkers(GoogleMap googleMap) {
        // Add the end marker
        if (showEndMarker) {
            for (int i = locations.size() - 1; i >= 0; i--) {
                CachedLocation cachedLocation = locations.get(i);
                if (cachedLocation.isValid()) {
                    MarkerOptions markerOptions = new MarkerOptions().position(cachedLocation.getLocation())
                            .anchor(MARKER_X_ANCHOR, MARKER_Y_ANCHOR).draggable(false).visible(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_red_paddle));
                    googleMap.addMarker(markerOptions);
                    break;
                }
            }
        }

        // Add the start marker
        boolean hasStartMarker = false;
        for (int i = 0; i < locations.size(); i++) {
            CachedLocation cachedLocation = locations.get(i);
            if (cachedLocation.isValid()) {
                MarkerOptions markerOptions = new MarkerOptions().position(cachedLocation.getLocation())
                        .anchor(MARKER_X_ANCHOR, MARKER_Y_ANCHOR).draggable(false).visible(true)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_green_paddle));
                googleMap.addMarker(markerOptions);
                hasStartMarker = true;
                break;
            }
        }
        return hasStartMarker;
    }


    /**
     * Updates the waypoints.
     *
     * @param googleMap the google map.
     */
    private void updateCheckRoutePoints(GoogleMap googleMap) {
        synchronized (lock) {
            for (RouteCheckPoint checkpoint : checkpoints) {
                Location location = checkpoint.getLocation();
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                int drawableId = R.drawable.ic_marker_blue_pushpin;
                MarkerOptions markerOptions = new MarkerOptions().position(latLng)
                        .anchor(WAYPOINT_X_ANCHOR, WAYPOINT_Y_ANCHOR).draggable(false).visible(true)
                        .icon(BitmapDescriptorFactory.fromResource(drawableId))
                        .title(String.valueOf(checkpoint.getId()));
                googleMap.addMarker(markerOptions);
            }
        }
    }
}
