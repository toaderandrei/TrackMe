package com.ant.track.app.activities;

import android.content.Context;

import com.ant.track.app.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class that contains all the drawer items.
 */
public class DrawerMenuContents {
    public static final String FIELD_TITLE = "title";
    public static final String FIELD_ICON = "icon";

    private ArrayList<Map<String, ?>> items;
    private Class[] activities;

    /**
     * this is a just a test. we can actually add fragments, etc.
     *
     * @param ctx
     */
    public DrawerMenuContents(Context ctx) {
        activities = new Class[2];
        items = new ArrayList<>(2);

        activities[0] = MainActivity.class;
        activities[1] = RouteDetailsActivity.class;
        items.add(populateDrawerItem(ctx.getString(R.string.main_activity), android.R.drawable.ic_menu_mapmode));
        items.add(populateDrawerItem(ctx.getString(R.string.route_list), android.R.drawable.ic_menu_info_details));
    }

    public List<Map<String, ?>> getItems() {
        return items;
    }

    public Class getActivity(int position) {
        return activities[position];
    }

    public int getPosition(Class activityClass) {
        for (int i = 0; i < activities.length; i++) {
            if (activities[i].equals(activityClass)) {
                return i;
            }
        }
        return -1;
    }

    private Map<String, ?> populateDrawerItem(String title, int icon) {
        HashMap<String, Object> item = new HashMap<>();
        item.put(FIELD_TITLE, title);
        item.put(FIELD_ICON, icon);
        return item;
    }
}
