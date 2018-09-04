package com.example.android.sunshine;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener{
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        // Layout for this fragment
        addPreferencesFromResource(R.xml.pref_visualizer);

        // Make PreferenceScreen and SharedPreference objects
        PreferenceScreen prefScreen = getPreferenceScreen();
        SharedPreferences sharedPref = prefScreen.getSharedPreferences();

        // Get the count of the all preferences
        int count = prefScreen.getPreferenceCount();

        // Go through each preference
        // Set the summary for preferences that are not CheckboxPreference
        for (int i = 0; i < count; i++) {
           Preference p = prefScreen.getPreference(i);
           if (!(p instanceof CheckBoxPreference)){
               setPrefSummaries(p, sharedPref.getString(p.getKey(), ""));
           }
        }

    }

    /**
     * Sets summary for given preference
     * @param preference
     * @param value
     */
    private void setPrefSummaries(Preference preference, String value) {
        // Just set the value if the preference is an EditTextPreference object.
        if (preference instanceof EditTextPreference){
            preference.setSummary(value);
        }
        /* If the preference is a ListPreference then find the correct entry fo given value
           and set the summary
         */
        else if (preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0){
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }


    /**
     * This method updates summaries of the preferences when they're changed
     * @param sharedPreferences
     * @param key
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /* When a preference other than CheckboxPreference changed, then update the summaries */
        // Find preference
        Preference preference = findPreference(key);
        if (preference != null){
            if (!(preference instanceof CheckBoxPreference)){
                setPrefSummaries(preference, sharedPreferences.getString(key, ""));
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register the PreferenceChangedListener
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Unregister the PreferenceChangedListener
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
