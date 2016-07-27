package com.ant.track.lib.service;

/**
 * Recording state
 */
public enum RecordingState {

    NOT_STARTED("not_started"),

    STARTING("starting"),

    STARTED("started"),

    STOPPED("stopped"),

    PAUSED("paused"),

    RESUMED("resumed");

    private String state;

    private RecordingState(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }
}
