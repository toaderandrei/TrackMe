package com.ant.track.app.maptools;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.List;

/**
 * class responsible for drawing the path.
 */
public class DrawPath {

    private static final String TAG = DrawPath.class.getCanonicalName();

    private Context context;
    private int color;

    public DrawPath(Context context, int routeColor) {
        this.context = context;
        this.color = context.getResources().getColor(android.R.color.black);
    }

    public int getColor() {
        return color;
    }

    public Context getContext() {
        return context;
    }

    /**
     * updates the current map with points
     *
     * @param mMap       the google map
     * @param startIndex the stard index.
     * @param locations  locations.
     * @param path       the existent path
     */
    public void updatePath(GoogleMap mMap, List<Polyline> path, int startIndex, List<CachedLocation> locations) {
        if (mMap == null) {
            Log.d(TAG, "map is null.");
            return;
        }

        if (startIndex >= locations.size()) {
            Log.d(TAG, "start index is bigger than locations size.");
            return;
        }

        boolean newRoute = startIndex == 0 || !locations.get(startIndex - 1).isValid();
        boolean useLastPolyline = true;
        List<LatLng> segmentPointsToBeInserted = new ArrayList<>();

        for (CachedLocation cachedLocation : locations) {
            if (!cachedLocation.isValid()) {
                newRoute = true;
                continue;
            }

            LatLng latLng = cachedLocation.getLocation();
            if (newRoute) {
                DrawPathUtils.addPath(mMap, segmentPointsToBeInserted, color, path, useLastPolyline);
                useLastPolyline = false;
                newRoute = false;
            }

            segmentPointsToBeInserted.add(latLng);
        }

        DrawPathUtils.addPath(mMap, segmentPointsToBeInserted, color, path, useLastPolyline);

    }
}
