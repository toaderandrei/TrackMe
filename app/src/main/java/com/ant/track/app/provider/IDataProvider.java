package com.ant.track.app.provider;

import android.location.Location;

/**
 * Created by Toader on 6/3/2015.
 */
public interface IDataProvider {


    /**
     * updates the current location to the server
     *
     * @param location the location to be updated to the server
     */
    void updateLocation(Location location);
}
