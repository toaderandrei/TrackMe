package com.ant.track.app.fragments;

import android.app.Activity;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.ant.track.app.R;
import com.ant.track.app.activities.TrackStateListener;
import com.ant.track.app.helper.GoogleAskToEnableLocationService;
import com.ant.track.app.helper.GoogleLocationServicesUtils;
import com.ant.track.app.location.GPSLiveTrackerLocationManager;
import com.ant.track.lib.models.User;
import com.ant.track.lib.publisher.ContentPublisher;
import com.ant.track.lib.publisher.INotifyUIListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

/**
 * Fragment in which a google map resides.
 */
public class LocationFragment extends Fragment implements INotifyUIListener {

    private static final float DEFAULT_ZOOM_LEVEL = 18f;
    private static final String TAG = LocationFragment.class.getSimpleName();
    private static final double DEFAULT_LATITUDE = 68.319392;
    private static final double DEFAULT_LONGITUDE = 14.407718;
    private GoogleMap mMap;
    private ImageButton myLocationImageButton;
    private LocationListener locationListener;
    private LocationSource.OnLocationChangedListener onLocationChangedListener;
    // Current location
    private Location currentLocation;
    private TrackStateListener listener;
    private GoogleAskToEnableLocationService enableLocationService;
    public static final int CONNECTION_RESOLUTION_CODE = 300;
    private GPSLiveTrackerLocationManager mGPSLiveTrackerLocManager;

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
            listener = (TrackStateListener) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.maps_layout, container, false);
        enableLocationService = new GoogleAskToEnableLocationService(getActivity());
        mGPSLiveTrackerLocManager = new GPSLiveTrackerLocationManager(getActivity());
        return rootView;
    }

    /**
     * in case the user wants to know its location and the gps is disabled then a notification will be shown
     */
    protected void notifyUserNoLocationIsAvailable() {
        String message = GoogleLocationServicesUtils.getGpsDisabledMyLocationMessage(getActivity());
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }


    private GoogleMap.OnMyLocationButtonClickListener getOnMyLocationClickListener() {
        return new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                requestLastLocationOrNotify();
                return true;
            }
        };
    }

    private void requestLastLocationOrNotify() {
        if (mGPSLiveTrackerLocManager != null && !GoogleLocationServicesUtils.isGpsProviderEnabled(getActivity())) {
            notifyUserNoLocationIsAvailable();
        } else {
            requestLastKnowLocation();
        }
    }

    private void requestLastKnowLocation() {
        if (mGPSLiveTrackerLocManager != null) {
            if (locationListener != null) {
                mGPSLiveTrackerLocManager.removeLocationUpdates(locationListener);
            }
            locationListener = null;
            initLocationListener();
            mGPSLiveTrackerLocManager.requestLastLocation(locationListener);
        }
    }

    public void updateCurrentLocation(Location location) {
        if (isResumed()) {
            setCurrentLocation(location);
            updateCurrentLocation(true);
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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initializeMapIfNeeded();
    }

    private void initializeMapIfNeeded() {
        if (mMap == null) {
            loadMapAsync();
        }
    }

    private void loadMapAsync() {
        ((SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.location_map)).getMapAsync(getOnMapReadyCallback());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        initGpsTracker(true);
    }

    public synchronized void initGpsTracker(boolean force) {
        if (mMap != null) {
            try {
                boolean isGPSProviderEnabled = GoogleLocationServicesUtils.isGpsProviderEnabled(getActivity());
                if (isGPSProviderEnabled) {
                    mMap.setMyLocationEnabled(true);
                } else if (force) {
                    askToEnableGps();
                }
            } catch (SecurityException secex) {
                Toast.makeText(getActivity(), "not enabled in manifest", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void askToEnableGps() {
        if (enableLocationService != null) {
            enableLocationService.askToEnableGps(locationCallback);
        }
    }

    private GoogleAskToEnableLocationService.GpsCallback locationCallback = new GoogleAskToEnableLocationService.GpsCallback() {
        @Override
        public void onSuccess() {
            initGpsTracker(false);
        }

        @Override
        public void onResolutionRequired(Status status) {
            try {
                status.startResolutionForResult(getActivity(), CONNECTION_RESOLUTION_CODE);
            } catch (IntentSender.SendIntentException setex) {
                Toast.makeText(getActivity(), "Exception in sending intent:", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError() {
            Toast.makeText(getActivity(), "Location services unavailable!", Toast.LENGTH_SHORT).show();
        }
    };

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


    public OnMapReadyCallback getOnMapReadyCallback() {
        return new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                mMap = map;
                if (mMap == null) {
                    return;
                }

                mMap.setIndoorEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                mMap.setOnMyLocationButtonClickListener(getOnMyLocationClickListener());
                mMap.setLocationSource(getNewLocationSource());
                mMap.setOnCameraChangeListener(getCameraChangeListener());
                initGpsTracker(true);
            }
        };
    }

    public void getLocationListener() {
        if (locationListener == null) {
            initLocationListener();
        }
    }

    public GoogleMap getGoogleMap() {
        return mMap;
    }

    /**
     * notifies the UI about a checking that was made.
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
}
