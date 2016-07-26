package com.ant.track.app.helper;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.ant.track.app.R;
import com.ant.track.app.fragments.LocationFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;

/**
 * For a brief explanation about what's happening in this class take a look here:
 * <br>
 *
 * @see <a href="https://android.googlesource.com/platform/packages/apps/Settings/+/ics-mr1/src/com/android/settings/GoogleLocationSettingHelper.java">GoogleLocationSettingHelper</a>
 */

public class GoogleLocationServicesUtils {

    // User has agreed to use location for Google services
    public static final int USE_LOCATION_FOR_SERVICES_ON = 1;
    private static final String TAG = GoogleLocationServicesUtils.class.getSimpleName();
    // Action to check if the google apps locations settings exists
    private static final String ACTION_GOOGLE_APPS_LOCATION_SETTINGS = "com.google.android.gsf.GOOGLE_APPS_LOCATION_SETTINGS";
    // User has disagreed to use location for Google services
    private static final int USE_LOCATION_FOR_SERVICES_OFF = 0;
    /*
     * The user has neither agreed nor disagreed to use location for Google
     * services yet.
     */
    private static final int USE_LOCATION_FOR_SERVICES_NOT_SET = 2;

    private static final String GOOGLE_SETTINGS_AUTHORITY = "com.google.settings";
    private static final Uri GOOGLE_SETTINGS_CONTENT_URI = Uri.parse("content://" + GOOGLE_SETTINGS_AUTHORITY + "/partner");
    private static final String NAME = "name";
    private static final String VALUE = "value";
    private static final String USE_LOCATION_FOR_SERVICES = "use_location_for_services";

    private GoogleApiClient locationClient;

    private Context context;
    private Handler handler;
    private boolean initialized;

    private static GoogleLocationServicesUtils instance;

    public GoogleLocationServicesUtils() {

    }


    public static GoogleLocationServicesUtils getInstance() {
        if (instance == null) {
            instance = new GoogleLocationServicesUtils();
        }
        return instance;
    }

    public void init(Context context) {
        this.handler = new Handler();
        this.context = context;
        locationClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();
        locationClient.connect();
        initialized = true;
    }


    private void warnUserPermissionNotSet(final String messageExc) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "Permission not given for location in manifest", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Error message: " + messageExc);
            }
        });
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
            //todo
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    /**
     * Gets the gps disabled message when my location button is pressed.
     */
    public String getGpsDisabledMyLocationMessage() {
        if (!initialized) {
            throw new IllegalStateException("not initialized");
        }
        int id = ApiHelper.hasLocationMode() ? R.string.gps_disabled_my_location_location_mode : R.string.gps_disabled_my_location;
        return context.getString(id, getLocationSettingsName());
    }

    /**
     * Gets the location settings name.
     **/
    private String getLocationSettingsName() {
        if (!initialized) {
            throw new IllegalStateException("not initialized");
        }
        return context.getString(
                useGoogleLocationSettings() ? R.string.gps_google_location_settings : R.string.gps_location_access);
    }

    /**
     * Returns true to use the google location settings.
     **/
    private boolean useGoogleLocationSettings() {
        if (!initialized) {
            throw new IllegalStateException("not initialized");
        }

        if (!ApiHelper.hasLocationMode()) {
            // Before KitKat
            return true;
        } else {
            // KitKat+
            return getUseLocationForServices() == USE_LOCATION_FOR_SERVICES_OFF;
        }
    }


    /* Returns true if there is no enforcement or google location settings allows
    * access.
    *
    * @param context the context
    */
    public boolean isAllowed() {
        if (!initialized) {
            return false;
        }
        if (!ApiHelper.hasLocationMode()) {
            // Before KitKat
            return getUseLocationForServices() == USE_LOCATION_FOR_SERVICES_ON;
        } else {
            // KitKat+
            return getUseLocationForServices() != USE_LOCATION_FOR_SERVICES_OFF;
        }
    }

    /**
     * Get the current value for the 'Use value for location' setting.
     *
     * @return One of {@link #USE_LOCATION_FOR_SERVICES_NOT_SET},
     * {@link #USE_LOCATION_FOR_SERVICES_OFF} or
     * {@link #USE_LOCATION_FOR_SERVICES_ON}.
     */
    private int getUseLocationForServices() {
        if (!initialized) {
            throw new IllegalStateException("not initialized");
        }
        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = null;
        String stringValue = null;
        try {
            cursor = contentResolver.query(GOOGLE_SETTINGS_CONTENT_URI, new String[]{VALUE},
                    NAME + "=?", new String[]{USE_LOCATION_FOR_SERVICES}, null);
            if (cursor != null && cursor.moveToNext()) {
                stringValue = cursor.getString(0);
            }
        } catch (RuntimeException e) {
            Log.w(TAG, "Failed to get 'Use My Location' setting", e);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (stringValue == null) {
            return USE_LOCATION_FOR_SERVICES_NOT_SET;
        }
        int value;
        try {
            value = Integer.parseInt(stringValue);
        } catch (NumberFormatException nfe) {
            value = USE_LOCATION_FOR_SERVICES_NOT_SET;
        }
        return value;
    }

    /**
     * Returns true if gps provider is enabled.
     */
    public boolean isGpsProviderEnabled() {
        if (!isAllowed()) {
            return false;
        }

        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        String provider = LocationManager.GPS_PROVIDER;
        try {

            if (locationManager.getProvider(provider) == null) {
                return false;
            }
            return locationManager.isProviderEnabled(provider);
        } catch (SecurityException secex) {
            return false;
        }
    }

    /**
     * Returns true if gps provider is enabled.
     */
    public void enableGPS(LocationFragment.GPSCallback callback) {
        // this.callback = callback;
        if (locationClient == null | !initialized) {
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

    private ResultCallback getResultCallback(final LocationFragment.GPSCallback callback) {

        return new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                final LocationSettingsStates statesResult = result.getLocationSettingsStates();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
                        callback.onConnected();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        callback.onAskToConnect(status);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        callback.onDisconnected();
                        break;
                }
            }
        };
    }

    LocationRequest mLocationRequestHighAccuracy = new LocationRequest();

    public boolean isInitialized() {
        return initialized;
    }
}
