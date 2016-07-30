package com.ant.track.app.maptools;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.model.RouteCheckPoint;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.utils.LocationUtils;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Class that deals with storing location points, markers,
 * start points, end points, etc.
 */
public class MapOverlay {

    private BlockingQueue<CachedLocation> pendingLocations = null;

    private static final String TAG = MapOverlay.class.getCanonicalName();
    private static final String ROUTE_COLOR = PreferenceUtils.DEFAULT_ROUTE_COLOR;
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
        if (location != null && pendingLocations.contains(location)) {
            pendingLocations.offer(new CachedLocation(location));
        } else {
            Log.i(TAG, "cannot insert the location or location is null");
        }
    }

    public void addPendingLocation(Location location) {
        if (location != null && pendingLocations.contains(location)) {
            pendingLocations.offer(new CachedLocation(location));
        } else {
            Log.i(TAG, "cannot insert the location or location is null");
        }
    }

    public void update(GoogleMap mMap, boolean reload) {
        synchronized (lock){
            int newLocations = pendingLocations.drainTo(locations);

            if(newLocations)
            {

            }
        }
    }

    /**
     * cached location that basically stores the location from Location object
     * to LatLng object.
     */
    private class CachedLocation {

        private LatLng location;
        //for now it is not used.
        private float speed;
        private boolean isValid;

        public CachedLocation(Location location) {
            isValid = LocationUtils.isValidLocation(location);
            this.location = new LatLng(location.getLatitude(), location.getLongitude());
            this.speed = location.getSpeed();
        }

        public CachedLocation(LatLng latLng) {
            isValid = LocationUtils.isValidLocation(latLng);
            this.location = latLng;
        }

        public double getSpeed() {
            return speed;
        }

        public LatLng getLocation() {
            return location;
        }

        public void setSpee(float speed) {
            this.speed = speed;
        }

        public boolean isValid() {
            return isValid;
        }
    }
}
