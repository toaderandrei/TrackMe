package com.ant.track.app.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.ant.track.app.R;
import com.ant.track.lib.constants.Constants;
import com.ant.track.lib.content.factory.TrackMeDatabaseUtilsImpl;
import com.ant.track.lib.model.Route;

/**
 * Details of a route.
 */
public class RouteDetailsFragment extends Fragment {

    private EditText edtName;
    private EditText edtDescription;

    private Route route;

    private RouteDetailsFragment.Callback callback;

    public static RouteDetailsFragment newInstance(long routeid) {
        RouteDetailsFragment routeDetailsFragment = new RouteDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putLong(Constants.EXTRA_ROUTE_ID_KEY, routeid);
        routeDetailsFragment.setArguments(bundle);
        return routeDetailsFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callback = (Callback) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            long routeid = getArguments().getLong(Constants.EXTRA_ROUTE_ID_KEY);
            route = TrackMeDatabaseUtilsImpl.getInstance().getRouteById(routeid);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.route_details_fragment, container, false);
        edtName = (EditText) view.findViewById(R.id.route_edit_name);
        edtDescription = (EditText) view.findViewById(R.id.route_edit_description);
        if (!TextUtils.isEmpty(route.getRouteName())) {
            edtName.setText(route.getRouteName());
        }

        if (!TextUtils.isEmpty(route.getDescription())) {
            edtDescription.setText(route.getDescription());
        }
        Button saveButton = (Button) view.findViewById(R.id.route_edit_save);
        saveButton.setOnClickListener(cancelSaveListener);
        Button cancelButton = (Button) view.findViewById(R.id.route_edit_cancel);
        saveButton.setOnClickListener(cancelSaveListener);
        cancelButton.setOnClickListener(cancelSaveListener);
        return view;
    }


    private View.OnClickListener cancelSaveListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view.getId() == R.id.route_edit_cancel) {
                callback.onFinish();
            } else if (view.getId() == R.id.route_edit_save) {
                if (edtName.getText() != null) {
                    String name = edtName.getText().toString();
                    if (!TextUtils.equals(name, route.getRouteName())) {
                        route.setRouteName(name);
                    }
                }
                //route.setRouteName(edtName.getText().toString());
                if (edtDescription.getText() != null) {
                    String desc = edtDescription.getText().toString();
                    if (!TextUtils.equals(desc, route.getDescription())) {
                        route.setDescription(desc);
                    }
                }
                TrackMeDatabaseUtilsImpl.getInstance().updateRouteTrack(route);
                callback.onSaved();
            }
        }
    };


    public interface Callback {

        /**
         * called after the save button has been called.
         */
        void onSaved();

        /**
         * calls whenever we want to kill the current activity
         */
        void onFinish();
    }
}
