package com.ant.track.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.ant.track.R;
import com.ant.track.activities.TrackStateListener;
import com.ant.track.application.GPSLiveTrackerApplication;

/**
 * Created by Toader on 6/1/2015.
 */
public class RecordControlsFragment extends Fragment {

    private ImageButton recordImageButton;
    private boolean isResumed = false;
    private TrackStateListener listener;
    private boolean isRecording;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (listener == null) {
            listener = (TrackStateListener) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record_controls, container, false);
        recordImageButton = (ImageButton) rootView.findViewById(R.id.play_pause_track);
        return rootView;
    }

    /**
     * updates the start/stop recording buttons.
     *
     * @param recording true enables the stop button, false otherwise.
     */
    public void update(boolean recording) {
        if (!isResumed) {
            return;
        }
        isRecording = recording;
        recordImageButton.setImageResource(isRecording ? R.drawable.ic_button_pause : R.drawable.button_record);
    }


    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
    }

    private GPSLiveTrackerApplication getApp() {
        return (GPSLiveTrackerApplication) GPSLiveTrackerApplication.getInstance();
    }

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
        update(isRecording);
    }


    /**
     * @param recordListener
     * @param stopListener
     */
    public void updateRecordListeners(View.OnClickListener recordListener, View.OnClickListener stopListener) {
        if (recordListener != null && recordImageButton != null) {
            recordImageButton.setOnClickListener(recordListener);
        }
    }
}
