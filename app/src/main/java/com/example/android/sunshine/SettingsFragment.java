package com.example.android.sunshine;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Layout for this fragment
        addPreferencesFromResource(R.xml.pref_visualizer);
    }
}
