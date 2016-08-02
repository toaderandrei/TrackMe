package com.ant.track.app.maptools;

import com.ant.track.lib.constants.Constants;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for drawing a route.
 */
public class DrawPathUtils {

    /**
     * add path.
     *
     * @param mMap            google map
     * @param points          last segement points
     * @param color           color of the path
     * @param paths           the existent path
     * @param useLastPolyline if to use or not the last polyline
     */
    public static void addPath(GoogleMap mMap, List<LatLng> points, int color, List<Polyline> paths, boolean useLastPolyline) {

        if (points.size() == 0) {
            return;
        }
        if (paths.size() != 0 && useLastPolyline) {
            Polyline lastPolyLine = paths.get(paths.size() - 1);
            List<LatLng> allPoints = new ArrayList<>();
            allPoints.addAll(lastPolyLine.getPoints());
            allPoints.addAll(points);
            lastPolyLine.setPoints(allPoints);
        } else {
            PolylineOptions polylineOptions = new PolylineOptions().addAll(points).width(Constants.DEFAULT_POLYLINE_POINT_WIDTH).color(color);
            Polyline polyline = mMap.addPolyline(polylineOptions);
            paths.add(polyline);

        }
        points.clear();
    }
}
