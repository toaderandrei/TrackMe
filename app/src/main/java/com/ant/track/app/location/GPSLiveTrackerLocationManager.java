package com.ant.track.app.location;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.ant.track.app.helper.GoogleLocationServicesUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

/**
 * Location Tracking Manager.
 */
public class GPSLiveTrackerLocationManager {

    private final GoogleApiClient locationClient;
    private final Handler handler;
    private final GoogleApiClient.OnConnectionFailedListener
            onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            //TODO notify...
        }
    };
    private boolean isAllowed;
    private LocationListener requestLastLocation;
    private LocationListener requestLocationUpdates;
    private float requestLocationUpdatesDistance;
    private long requestLocationUpdatesTime;
    private static final String TAG = GPSLiveTrackerLocationManager.class.getCanonicalName();
    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        if (requestLastLocation != null && locationClient.isConnected()) {
                            requestLastLocation.onLocationChanged(LocationServices.FusedLocationApi.getLastLocation(locationClient));
                            requestLastLocation = null;
                        }
                        if (requestLocationUpdates != null && locationClient.isConnected()) {
                            LocationRequest locationRequest = new LocationRequest()
                                    .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY).setInterval(requestLocationUpdatesTime)
                                    .setFastestInterval(requestLocationUpdatesTime)
                                    .setSmallestDisplacement(requestLocationUpdatesDistance);
                            LocationServices.FusedLocationApi.requestLocationUpdates(locationClient, locationRequest, requestLocationUpdates);
                        }
                    } catch (SecurityException secex) {
                        Log.e(TAG, "security exception problem:" + secex.getMessage());
                    }
                }
            });
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    public GPSLiveTrackerLocationManager(Context context) {
        this.handler = new Handler(Looper.getMainLooper());

        locationClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();
        locationClient.connect();
        isAllowed = GoogleLocationServicesUtils.getInstance(context).isAllowed();

    }


    /**
     * Request last location.
     *
     * @param locationListener location listener
     */
    public void requestLastLocation(final LocationListener locationListener) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (!isAllowed()) {
                    requestLastLocation = null;
                    locationListener.onLocationChanged(null);
                } else {
                    requestLastLocation = locationListener;
                    connectionCallbacks.onConnected(null);
                }
            }
        });
    }

    /**
     * Requests location updates. This is an ongoing request, thus the caller
     * needs to check the status of {@link #isAllowed}.
     *
     * @param minTime          the minimal time
     * @param minDistance      the minimal distance
     * @param locationListener the location listener
     */
    public void requestLocationUpdates(final long minTime, final float minDistance, final LocationListener locationListener) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLocationUpdatesTime = minTime;
                requestLocationUpdatesDistance = minDistance;
                requestLocationUpdates = locationListener;
                connectionCallbacks.onConnected(null);
            }
        });
    }

    /**
     * Removes location updates.
     *
     * @param locationListener the location listener
     */
    public void removeLocationUpdates(final LocationListener locationListener) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                requestLocationUpdates = null;
                if (locationClient != null && locationClient.isConnected()) {
                    LocationServices.FusedLocationApi.removeLocationUpdates(locationClient, locationListener);
                }
            }
        });
    }


    /**
     * Returns true if allowed to access the location manager. Returns true if
     * there is no enforcement or the Google location settings allows access to
     * location data.
     */
    public boolean isAllowed() {
        return isAllowed;
    }

    /**
     * Closes the {@link GPSLiveTrackerLocationManager}.
     */
    public void close() {
        if (locationClient != null) {
            locationClient.disconnect();
        }
    }
}
