package com.ant.track.app.fragments;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ant.track.app.R;

/**
 * Fragment that deals with preferences.
 */
public class CustomPreferenceFragment extends PreferenceFragment {


    public static CustomPreferenceFragment newInstance() {
        CustomPreferenceFragment customPreferenceFragment = new CustomPreferenceFragment();
        return customPreferenceFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);
    }

}
