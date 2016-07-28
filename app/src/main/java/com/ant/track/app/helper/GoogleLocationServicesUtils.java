package com.ant.track.app.helper;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.ant.track.app.R;
import com.google.android.gms.common.api.GoogleApiClient;

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

    private GoogleLocationServicesUtils(Context context) {
        this.context = context;
        initialized = true;
    }


    public static GoogleLocationServicesUtils getInstance(Context context) {
        if (instance == null) {
            instance = new GoogleLocationServicesUtils(context);
        }
        return instance;
    }

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
            Log.e(TAG, "exception in getting location");
            return false;
        }
    }
}
