package com.ant.track.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ant.track.R;
import com.ant.track.activities.IRecordStateListener;
import com.ant.track.helper.GoogleLocationServicesUtils;
import com.ant.track.location.GPSLiveTrackerLocationManager;
import com.ant.track.models.User;
import com.ant.track.publisher.ContentPublisher;
import com.ant.track.publisher.INotifyUIListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Toader on 6/1/2015.
 */
public class LocationFragment extends Fragment implements INotifyUIListener {

    private static final float DEFAULT_ZOOM_LEVEL = 18f;
    private static final String TAG = LocationFragment.class.getSimpleName();
    private static final double DEFAULT_LATITUDE = 68.319392;
    private static final double DEFAULT_LONGITUDE = 14.407718;
    private GoogleMap mMap;
    private ImageButton myLocationImageButton;
    private LocationListener locationListener;
    private GPSLiveTrackerLocationManager mGPSLiveTrackerLocManager;
    private LocationSource.OnLocationChangedListener onLocationChangedListener;
    // Current location
    private Location currentLocation;
    private IRecordStateListener listener;

    public static LocationFragment newInstance(boolean isRecording) {
        LocationFragment f = new LocationFragment();
        Bundle args = new Bundle();
        args.putBoolean("is_recording", isRecording);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        registerToContentPublisher();
    }

    private void registerToContentPublisher() {
        ContentPublisher.getInstance().registerListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (this.listener == null) {
            listener = (IRecordStateListener) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.maps_layout, container, false);
        setMyLocationButton(rootView);
        return rootView;
    }

    private void setMyLocationButton(View rootView) {
        myLocationImageButton = (ImageButton) rootView.findViewById(R.id.map_my_location);
        myLocationImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mGPSLiveTrackerLocManager != null && !mGPSLiveTrackerLocManager.isGpsProviderEnabled()) {
                    notifyUserNoLocationIsAvailable();
                } else {
                    initOrUpdateGPSLiveTrackingManager();
                }
            }
        });
    }

    /**
     * in case the user wants to know its location and the gps is disabled then a notification will be shown
     */
    protected void notifyUserNoLocationIsAvailable() {
        String message = GoogleLocationServicesUtils.getGpsDisabledMyLocationMessage(getActivity());
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Initializes the gps live tracking manager. Initially it checks if there was a location listener. If yes,
     * it removes all the updates and starts fresh.
     */
    protected void initOrUpdateGPSLiveTrackingManager() {
        if (locationListener != null) {
            mGPSLiveTrackerLocManager.removeLocationUpdates(locationListener);
            locationListener = null;
        }
        if (isSelectedLiveTracking()) {
            mGPSLiveTrackerLocManager.requestLastLocation(new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    if (isResumed()) {
                        setCurrentLocation(location);
                        updateCurrentLocation(true);
                    }
                }
            });
        } else {
            initLocationListener();
            /*
             * Set currentLocation to null to cause the first requested location
             * to force zoom to the default level.
             */
            currentLocation = null;
            mGPSLiveTrackerLocManager.requestLocationUpdates(0, 0f, locationListener);
        }
    }

    private void initLocationListener() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (isResumed()) {
                    boolean isFirst = setCurrentLocation(location);
                    updateCurrentLocation(isFirst);
                }
            }
        };
    }


    /**
     * Sets the current location.
     *
     * @param location the location
     * @return true if this is the first location
     */
    protected boolean setCurrentLocation(Location location) {
        boolean isFirst = false;
        if (currentLocation == null && location != null) {
            isFirst = true;
        }
        currentLocation = location;
        return isFirst;
    }


    /**
     * Updates the current location.
     *
     * @param forceZoom true to force zoom to the current location regardless of
     *                  the keepCurrentLocationVisible policy
     */
    protected void updateCurrentLocation(final boolean forceZoom) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                updateLocationInternal(forceZoom);
            }
        });
    }

    /**
     * updates the current location according to the forceZoom parameter.
     * Technically speaking, the forceZoom is the first time the my location button is triggered, or, in case of a resume.
     *
     * @param forceZoom true force, false otherwise
     */
    private void updateLocationInternal(boolean forceZoom) {
        if (!isResumed() || getGoogleMap() == null || onLocationChangedListener == null
                || currentLocation == null) {
            Log.d(TAG, "isResumed or google map or locationchanged listener or current location is null");
            return;
        }
        onLocationChangedListener.onLocationChanged(currentLocation);
        if (forceZoom || !isLocationVisible(currentLocation)) {
            LatLng latLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
            CameraUpdate cameraUpdate = forceZoom ? CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL) : CameraUpdateFactory.newLatLng(latLng);
            getGoogleMap().animateCamera(cameraUpdate);
        }
    }

    /**
     * Returns true if the location is visible. Needs to run on the UI thread.
     *
     * @param location the location
     */
    protected boolean isLocationVisible(Location location) {
        if (location == null || getGoogleMap() == null) {
            return false;
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        return getGoogleMap().getProjection().getVisibleRegion().latLngBounds.contains(latLng);
    }


    /**
     * checks if the recording service has started.
     *
     * @return true if yes, false if not.
     */
    protected boolean isSelectedLiveTracking() {
        if (this.listener != null) {
            return this.listener.isRecording();
        }
        return false;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeMapIfNeeded();
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mGPSLiveTrackerLocManager = new GPSLiveTrackerLocationManager(getActivity(), Looper.myLooper(), true);
        boolean isGpsProviderEnabled = mGPSLiveTrackerLocManager.isGpsProviderEnabled();

        if (getGoogleMap() != null) {
            // Disable my location if gps is disabled
            getGoogleMap().setMyLocationEnabled(isGpsProviderEnabled);
        }
        if (currentLocation != null && isSelectedLiveTracking()) {
            updateCurrentLocation(true);
        } else {
            if (onLocationChangedListener != null) {
                onLocationChangedListener.onLocationChanged(getDefaultLocation());
            }
        }
    }

    protected void initializeMapIfNeeded() {
        if (mMap == null) {
            mMap = getMap();
            if (mMap == null) {
                return;
            }
           /*
           *  Currently, the Google Maps API doesn't allow handling onClick event,
           * thus hiding the default my location button and providing our own.
           */
            mMap.setIndoorEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.setLocationSource(getNewLocationSource());
            mMap.setOnCameraChangeListener(getCameraChangeListener());
        }
    }

    private GoogleMap.OnCameraChangeListener getCameraChangeListener() {
        return new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition cameraPosition) {
                if (isResumed() && currentLocation != null && !isLocationVisible(currentLocation)) {
                    //TODO
                }
            }
        };
    }

    protected LocationSource getNewLocationSource() {
        return new LocationSource() {

            @Override
            public void activate(OnLocationChangedListener listener) {
                onLocationChangedListener = listener;
            }

            @Override
            public void deactivate() {
                onLocationChangedListener = null;
            }
        };
    }

    @Override
    public void onStop() {
        super.onStop();
        if (locationListener != null) {
            mGPSLiveTrackerLocManager.removeLocationUpdates(locationListener);
            locationListener = null;
        }
        mGPSLiveTrackerLocManager.close();
        unregisterFromContentPublisher();
    }

    private void unregisterFromContentPublisher() {
        ContentPublisher.getInstance().unregisterListener(this);
    }


    private GoogleMap getMap() {
        return ((MapFragment) getFragmentManager().findFragmentById(R.id.location_map)).getMap();
    }

    public GoogleMap getGoogleMap() {
        return mMap;
    }

    /**
     * notifies the UI about a checkin that was made.
     *
     * @param user for which to show the notification.
     */
    @Override
    public void notifyUI(final User user) {
        Location locFromServer = user.getUserAddress().getLocation();
        if (locFromServer != null) {
            this.currentLocation = locFromServer;
            updateCurrentLocation(true);
            this.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), "Address:" + user.getUserAddress().getAddress(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * return a default location.
     *
     * @return the default location
     */
    protected Location getDefaultLocation() {

        Location loc = new Location("");
        loc.setLatitude(DEFAULT_LATITUDE);
        loc.setLongitude(DEFAULT_LONGITUDE);
        return loc;
    }

    public LocationListener getLocationListener() {
        if (locationListener == null) {
            initLocationListener();
        }
        return locationListener;
    }
}
