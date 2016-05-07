package com.ant.track.helper;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.lang.ref.WeakReference;

/**
 * Class that deals with requests for enabling the location service.
 */
public class GoogleAskToEnableLocationService {

    private LocationRequest mLocationRequestHighAccuracy = new LocationRequest();
    private static final String TAG = GoogleAskToEnableLocationService.class.getCanonicalName();
    private GoogleApiClient locationClient;
    private Handler handler;

    private WeakReference<Context> contextWeakReference;

    public GoogleAskToEnableLocationService(Context context) {
        this.handler = new Handler();
        contextWeakReference = new WeakReference<>(context);
        locationClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();
        locationClient.connect();
    }

    private final GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            String message = connectionResult.getErrorMessage();
            warnUserPermissionNotSet(message);
        }
    };

    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {

        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private void warnUserPermissionNotSet(final String messageExc) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(contextWeakReference.get(), "Permission not given for location in manifest", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error message: " + messageExc);
            }
        });
    }

    public void askToEnableGps(final GpsCallback callback) {
        if (locationClient == null) {
            return;
        }
        mLocationRequestHighAccuracy = LocationRequest.create();
        mLocationRequestHighAccuracy.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequestHighAccuracy.setInterval(30 * 1000);
        mLocationRequestHighAccuracy.setFastestInterval(5 * 1000);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequestHighAccuracy);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(locationClient, builder.build());

        result.setResultCallback(getResultCallback(callback));
    }

    private ResultCallback<LocationSettingsResult> getResultCallback(final GpsCallback callback) {

        return new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                if (result != null) {
                    final Status status = result.getStatus();
                    if (callback != null) {
                        //final LocationSettingsStates statesResult = result.getLocationSettingsStates();
                        switch (status.getStatusCode()) {
                            case LocationSettingsStatusCodes.SUCCESS:
                                // All location settings are satisfied. The client can initialize location
                                // requests here.
                                callback.onSuccess();
                                break;
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                // Location settings are not satisfied. But could be fixed by showing the user
                                // a dialog.
                                callback.onResolutionRequired(status);
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                // Location settings are not satisfied. However, we have no way to fix the
                                // settings so we won't show the dialog.
                                callback.onError();
                                break;
                        }
                    }
                }
            }
        };
    }

    public interface GpsCallback {
        /**
         * callback method for when the result it is a success.
         */
        void onSuccess();

        /**
         * callback for when user interaction it is required.
         *
         * @param status the result from the service needed for the resolution.
         */
        void onResolutionRequired(Status status);

        /**
         * Callback for when the change it is not possible.
         */
        void onError();
    }

}
