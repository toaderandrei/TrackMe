package com.ant.track.app.service;

import android.location.Location;

/**
 * ErrorLocation class that contains aside from exception the location where it happened.
 * */
public class ErrorLocation extends Exception {

    private Location errorLocation;
    public ErrorLocation(Throwable throwable, Location location) {
        super(throwable);
        this.errorLocation = location;
    }
    public ErrorLocation(String message, Throwable throwable) {
        super(message, throwable);
    }

    public Location location(){
        return errorLocation;
    }
}
