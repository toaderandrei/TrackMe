package com.ant.track.app.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.ant.track.app.R;
import com.ant.track.app.activities.RecordStateListener;
import com.ant.track.lib.prefs.PreferenceUtils;
import com.ant.track.lib.service.RecordingState;

/**
 * Fragment containing the controls for starting/stopping a service.
 */
public class RecordControlsFragment extends Fragment {
    
    private static final String TAG = RecordControlsFragment.class.getCanonicalName();
    private ImageButton recordImageButton;
    private RecordStateListener listener;
    private static final String CUSTOM_TAG = "custom_tag";
    private RecordingState recordingState = RecordingState.NOT_STARTED;
    private ImageButton stopImageButton;
    private ImageButton lockImageButton;
    private boolean isLocked = false;
    
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
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_record_controls, container, false);
        recordImageButton = (ImageButton) rootView.findViewById(R.id.play_pause_track);
        stopImageButton = (ImageButton) rootView.findViewById(R.id.stop_track);
        recordImageButton.setOnClickListener(recordListener);
        stopImageButton.setOnClickListener(stopRecordListener);
        lockImageButton = (ImageButton) rootView.findViewById(R.id.lock_track);
        lockImageButton.setOnClickListener(lockRecordListener);
        return rootView;
    }
    
    
    /**
     * updates the start/stop recording buttons.
     * if it is started  - started -> pause image
     * if a long press occurs while recording, the user
     * will be able either to stop it or pause it.
     *
     * @param state - the state to be updated.
     */
    public void updateRecordState(RecordingState state) {
        
        if (isLocked) {
            Log.d(TAG, "it is locked.");
            return;
        }
        this.recordingState = state;
        int textId = R.string.image_record;
        int stopTextId = R.string.image_stop;
        
        int stopResid = R.drawable.ic_button_stop;
        int resId = R.drawable.ic_button_record;
        
        if (state == RecordingState.STARTED) {
            textId = R.string.image_pause;
            resId = R.drawable.ic_button_pause;
        } else if (state == RecordingState.RESUMED) {
            textId = R.string.image_record;
            resId = R.drawable.ic_button_record;
        } else if (state == RecordingState.STOPPED) {
            textId = R.string.image_record;
            resId = R.drawable.ic_button_record;
            stopResid = R.drawable.ic_button_stop_pressed;
            stopTextId = R.string.image_stop;
        }
        recordImageButton.setImageResource(resId);
        stopImageButton.setImageResource(stopResid);
        stopImageButton.setContentDescription(getActivity().getString(stopTextId));
        recordImageButton.setContentDescription(getActivity().getString(textId));
    }
    
    private View.OnClickListener stopRecordListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (view != null) {
                if (!isLocked) {
                    updateService(RecordingState.STOPPED);
                }
            }
        }
    };
    
    private View.OnClickListener lockRecordListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            updateLockControlsInternal(!isLocked);
        }
    };
    
    private void updateLockControlsInternal(boolean locked) {
        lockImageButton.setImageResource(locked ? R.drawable.ic_locked_recording : R.drawable.ic_unlocked_recording);
        isLocked = locked;
        PreferenceUtils.setBoolean(getActivity(), R.string.recording_locked_key, isLocked);
    }
    
    private View.OnClickListener recordListener = new View.OnClickListener() {
        
        @Override
        public void onClick(View view) {
            if (view != null) {
                if (!isLocked) {
                    if (recordingState == RecordingState.NOT_STARTED) {
                        updateService(RecordingState.STARTING);
                    } else if (recordingState == RecordingState.STARTED) {
                        updateService(RecordingState.PAUSED);
                    } else if (recordingState == RecordingState.PAUSED) {
                        updateService(RecordingState.RESUMED);
                    }
                }
            }
        }
    };
    
    
    private void updateService(RecordingState state) {
        listener.updateServiceState(state);
    }
    
    public void updateLockControls(boolean lock) {
        isLocked = lock;
        updateLockControlsInternal(isLocked);
    }
}
