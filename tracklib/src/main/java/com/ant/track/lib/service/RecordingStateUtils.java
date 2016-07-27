package com.ant.track.lib.service;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by toaderandrei on 27/07/16.
 */
public class RecordingStateUtils {

    private static Map<RecordingState, String> stateToStringMap = new HashMap<>();

    static Map<String, RecordingState> stringRecordingStateMap = new HashMap<>();

    static {
        for (RecordingState state : EnumSet.allOf(RecordingState.class)) {
            stateToStringMap.put(state, state.getState());
            stringRecordingStateMap.put(state.getState(), state);
        }
    }


    public static String getState(RecordingState state) {
        return stateToStringMap.get(state);
    }

    public static RecordingState getString(String key) {
        return stringRecordingStateMap.get(key);
    }
}
