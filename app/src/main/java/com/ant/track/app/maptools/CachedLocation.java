package com.ant.track.app.maptools;

import android.location.Location;

import com.ant.track.lib.utils.LocationUtils;
import com.google.android.gms.maps.model.LatLng;

/**
 * cached location that basically stores the location from Location object
 * to LatLng object.
 */
class CachedLocation {

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

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public boolean isValid() {
        return isValid;
    }
}
