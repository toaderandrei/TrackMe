package com.ant.track.app.activities;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.preference.PreferenceActivity;

import com.ant.track.app.fragments.CustomPreferenceFragment;

/**
 * Activity for the preferences.
 */
public class SettingsActivity extends PreferenceActivity {

    private static final String TAG = SettingsActivity.class.getCanonicalName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragment();
    }


    private void initFragment() {
        Fragment fragment = getFragmentManager().findFragmentByTag(TAG);
        if (fragment == null) {
            CustomPreferenceFragment customPreferenceFragment = CustomPreferenceFragment.newInstance();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(customPreferenceFragment, TAG);
            transaction.commit();
        }
    }

}
