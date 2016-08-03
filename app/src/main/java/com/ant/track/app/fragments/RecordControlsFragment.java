package com.ant.track.app.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.ant.track.app.R;
import com.ant.track.app.activities.RecordStateListener;
import com.ant.track.lib.service.RecordingState;
import com.ant.track.ui.dialogs.CustomFragmentDialog;

/**
 * Fragment containing the controls for starting/stopping a service.
 */
public class RecordControlsFragment extends Fragment {

    public static final String STOPS_THE_TRACKING_OR_PAUSES_IT = "Stops the tracking or pauses it.";
    public static final String STOP_OR_PAUSE = "Stop or pause?";
    private ImageButton recordImageButton;
    private static final String STOP_OR_CANCEL = "Stop or cancel?";
    private static final String STOPS_THE_TRACKING_OR_CANCEL = "Want to stop the route tracking?";
    private RecordStateListener listener;
    private int totalTime;
    private static final String CUSTOM_TAG = "custom_tag";
    private Handler handler;
    private RecordingState recordingState = RecordingState.NOT_STARTED;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (listener == null) {
            listener = (RecordStateListener) getActivity();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record_controls, container, false);
        recordImageButton = (ImageButton) rootView.findViewById(R.id.play_pause_track);
        recordImageButton.setOnClickListener(recordListener);

        return rootView;
    }

    /**
     * updates the start/stop recording buttons.
     * if it is started  - started -> pause image
     * if a long press occurs while recording, the user
     * will be able either to stop it or pause it.
     *
     * @param state -
     */
    public void updateRecordState(RecordingState state) {
        if (!isResumed()) {
            return;
        }

        this.recordingState = state;
        int textId = R.string.image_record;
        int resId = R.drawable.ic_button_record;
        if (state == RecordingState.STARTED || state == RecordingState.RESUMED) {
            textId = R.string.image_pause;
            resId = R.drawable.ic_button_pause;
        } else if (state == RecordingState.STOPPED || state == RecordingState.PAUSED) {
            textId = R.string.image_record;
            resId = R.drawable.ic_button_record;
        }
        recordImageButton.setImageResource(resId);
        recordImageButton.setContentDescription(getActivity().getString(textId));
    }

    public View.OnLongClickListener getOnLongClickListener() {
        return new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (view != null) {
                    if (recordingState == RecordingState.PAUSED || recordingState == RecordingState.STARTED) {
                        CustomFragmentDialog stopOrCancelDialog = CustomFragmentDialog.newInstance(STOP_OR_CANCEL,
                                STOPS_THE_TRACKING_OR_CANCEL,
                                getString(R.string.stop_tracking),
                                getString(R.string.cancel),
                                stopOrCancelCallback);
                        stopOrCancelDialog.show(getFragmentManager(), CUSTOM_TAG);
                    }
                }
                return false;
            }
        };
    }

    private CustomFragmentDialog.Callback stopOrCancelCallback = new CustomFragmentDialog.Callback() {
        @Override
        public void onPositiveButtonClicked(Bundle bundle) {
            updateService(RecordingState.STOPPED);
        }

        @Override
        public void onNegativeButtonClicked(Bundle bundle) {
            //nothing for now.
        }
    };

    private View.OnClickListener recordListener = new View.OnClickListener() {

        @Override
        public void onClick(View view) {
            if (view != null) {
                if (recordingState == RecordingState.NOT_STARTED || recordingState == RecordingState.STOPPED) {
                    updateService(RecordingState.STARTING);
                } else if (recordingState == RecordingState.STARTED || recordingState == RecordingState.RESUMED) {
                    CustomFragmentDialog customFragmentDialog = CustomFragmentDialog.newInstance(STOP_OR_PAUSE,
                            STOPS_THE_TRACKING_OR_PAUSES_IT,
                            getString(R.string.dialog_stop_tracking),
                            getString(R.string.dialog_pause_tracking),
                            customDialogCallback);
                    customFragmentDialog.show(getFragmentManager(), CUSTOM_TAG);
                } else if (recordingState == RecordingState.PAUSED) {
                    updateService(RecordingState.RESUMED);
                }
            }
        }
    };


    CustomFragmentDialog.Callback customDialogCallback = new CustomFragmentDialog.Callback() {
        @Override
        public void onPositiveButtonClicked(Bundle bundle) {
            //stop
            updateService(RecordingState.STOPPED);
        }

        @Override
        public void onNegativeButtonClicked(Bundle bundle) {
            //pause
            updateService(RecordingState.PAUSED);
        }
    };

    private void updateService(RecordingState state) {
        updateRecordState(state);
        listener.updateServiceState(state);
    }

    public RecordingState getRecordState() {
        return recordingState;
    }

    /**
     * this should be replaced by the preferences.
     *
     * @param state
     */
    public void setRecordingState(RecordingState state) {
        this.recordingState = state;
    }
}
