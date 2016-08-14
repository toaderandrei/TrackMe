package com.ant.track.app.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ant.track.app.R;
import com.ant.track.lib.db.provider.TrackMeContract;
import com.ant.track.lib.utils.TimeUtils;

/**
 * Adapter that shows a list of routes.
 */
public class RouteListAdapter extends CursorAdapter {

    public static class ViewHolder {
        public final TextView routeName;
        public final TextView routeDescription;
        public final TextView routeTotalTime;
        public final TextView routeTotalDistance;

        public ViewHolder(View view) {
            routeName = (TextView) view.findViewById(R.id.tv_route_name);
            routeDescription = (TextView) view.findViewById(R.id.tv_route_desc);
            routeTotalTime = (TextView) view.findViewById(R.id.route_total_time);
            routeTotalDistance = (TextView)view.findViewById(R.id.tv_total_distance);

        }
    }

    public RouteListAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, flags);
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.route_listitem_layout, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setTag(holder);
        return view;
    }

    @Override
    public int getCount() {
        if (getCursor() == null) {
            return 0;
        }
        return super.getCount();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String name = cursor.getString(cursor.getColumnIndex(TrackMeContract.RouteEntry.NAME));
        if (!TextUtils.isEmpty(name)) {
            holder.routeName.setText(name);
        }
        String description = cursor.getString(cursor.getColumnIndex(TrackMeContract.RouteEntry.DESCRIPTION));

        if (!TextUtils.isEmpty(description)) {
            holder.routeDescription.setText(description);
        }
        int totalTime = cursor.getInt(cursor.getColumnIndex(TrackMeContract.RouteEntry.TOTAL_TIME));
        String stringTotalTime = TimeUtils.getTotalTime(totalTime);

        String stringTotalDistance = cursor.getString(cursor.getColumnIndex(TrackMeContract.RouteEntry.TOTAL_DISTANCE));
        holder.routeTotalTime.setText(stringTotalTime);
        holder.routeTotalDistance.setText(stringTotalDistance);
    }
}
