package com.ant.track.app.maptools;

import android.content.Context;

/**
 * class responsible for drawing the path.
 */
public class DrawPath {

    private Context context;
    private String color;

    public DrawPath(Context context, String routeColor) {
        this.context = context;
        this.color = routeColor;
    }

    public String getColor() {
        return color;
    }

    public Context getContext() {
        return context;
    }
}
