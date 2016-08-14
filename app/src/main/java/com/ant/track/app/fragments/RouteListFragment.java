package com.ant.track.app.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ant.track.app.R;
import com.ant.track.app.adapters.RouteListAdapter;
import com.ant.track.lib.db.provider.TrackMeContract;

/**
 * List containing all the routes from the db.
 */
public class RouteListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = RouteListFragment.class.getCanonicalName();
    public static final String START_SERVICE_KEY = "start_service";
    private ListView mListView;
    private LinearLayout mErrorView;
    private TextView mErrorMessage;

    private ProgressBar mProgressBarView;
    private RouteListFragment.Callback callback;
    private RouteListAdapter routeCursorAdapter;
    public static final int LOADER_ID = 1000;

    public static RouteListFragment newInstance() {
        RouteListFragment repositoryFragment = new RouteListFragment();
        return repositoryFragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        callback = (Callback) getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.route_list_layout, container, false);
        mListView = (ListView) view.findViewById(R.id.list);
        mErrorView = (LinearLayout) view.findViewById(R.id.error_view);
        mErrorMessage = (TextView) view.findViewById(R.id.error_message);
        mProgressBarView = (ProgressBar) view.findViewById(R.id.progress_bar);
        routeCursorAdapter = new RouteListAdapter(getActivity(), null, 0);
        mListView.setAdapter(routeCursorAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);
        mListView.setOnItemLongClickListener(getOnLongClickListener());
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mErrorView.setVisibility(View.GONE);
        mProgressBarView.setVisibility(View.VISIBLE);

        return new CursorLoader(getActivity(),
                TrackMeContract.RouteEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(START_SERVICE_KEY, false);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
        mProgressBarView.setVisibility(View.GONE);
        mErrorMessage.setVisibility(View.GONE);
        routeCursorAdapter.swapCursor(newCursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        routeCursorAdapter.swapCursor(null);
    }

    private AdapterView.OnItemLongClickListener getOnLongClickListener() {
        return new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View arg1,
                                           int pos, long id) {
                Log.v("long clicked", "pos: " + pos);
                Cursor adapterCursor = null;
                try {
                    adapterCursor = (Cursor) parent.getItemAtPosition(pos);
                    long routeId = adapterCursor.getLong(adapterCursor.getColumnIndex(TrackMeContract.RouteEntry._ID));
                    if (routeId != -1L) {
                        callback.onViewDetails(routeId);
                    }
                } finally {
                    if (adapterCursor != null) {
                        //adapterCursor.close();
                    }
                }
                return true;
            }
        };
    }

    public interface Callback {
        /**
         * callback for when the user clicks on a list item
         *
         * @param id the id of the route.
         */
        void onViewDetails(long id);
    }
}
