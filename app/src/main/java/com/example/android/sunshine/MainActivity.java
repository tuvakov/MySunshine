/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.sunshine;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.sunshine.ForecastAdapter.ForecastAdapterOnClickHandler;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.database.AppDatabase;
import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;
import com.example.android.sunshine.utilities.SunshineDateUtils;
import com.example.android.sunshine.utilities.SunshineSyncUtils;
import com.example.android.sunshine.utilities.SunshineWeatherUtils;

import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ForecastAdapterOnClickHandler,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private final String TAG = this.getClass().getSimpleName();
    private static boolean UNIT_PREFERENCE_UPDATED = false;


    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_forecast);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);

        /*
         * LinearLayoutManager can support HORIZONTAL or VERTICAL orientations. The reverse layout
         * parameter is useful mostly for HORIZONTAL layouts that should reverse for right to left
         * languages.
         */
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * The ForecastAdapter is responsible for linking our weather data with the Views that
         * will end up displaying our weather data.
         */
        mForecastAdapter = new ForecastAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mForecastAdapter);

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* Start sync */
        SunshineSyncUtils.startImmediateSync(this.getApplicationContext());

        /* Loading the data from view model*/
        loadDataFromViewModel();

        // Register the OnSharedPreferencesClickListener
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        /* If a preference updated then reload the data and reset the flag */
        if (UNIT_PREFERENCE_UPDATED) {
            /* Notify the adapter since we've changed units */
            mForecastAdapter.notifyDataSetChanged();
            UNIT_PREFERENCE_UPDATED = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the OnSharedPreferencesClickListener
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param weatherEntryId The weather entry id for the day that was clicked
     */
    @Override
    public void onClick(int weatherEntryId) {
        Context context = this;
        /* Start DetailActivity */
        // Make an intent object
        Intent intent = new Intent(context, DetailActivity.class);
        // Put the weather data
        intent.putExtra(DetailActivity.INTENT_ID_KEY, weatherEntryId);
        // Start activity
        startActivity(intent);
    }

    /**
     * This method will make the View for the weather data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showWeatherDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the weather data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the weather
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void openMapLocation() {
        // Get location from SharedPreferences
        String address = SunshinePreferences.getPreferredWeatherLocation(this);

        // Parsing the address to an Uri object
        Uri locationUri = Uri.parse("geo:0,0?q=" + address);

        // Making an Intent object and set location
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(locationUri);

        // Make sure there is a map app to handle this intent and start the intent
        if (intent.resolveActivity(getPackageManager()) != null){
            startActivity(intent);
        } else {
            Log.e(TAG, "Couldn't start map intent");
        }
    }


    /* Loads data from the MainViewModel */
    private void loadDataFromViewModel(){
        // Get the MainViewModel
        MainViewModel mainViewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        // Get the weather data
        final LiveData<List<WeatherEntry>> weatherEntries = mainViewModel.getWeatherEntries();

        /* Assign an Observer to the LiveData object
         * Update UI when the data changes.
         * The data changed supposed to happen when the SyncIntentService updates DB
         */
        weatherEntries.observe(this, entries -> {
            // Update UI
            mForecastAdapter.setWeatherData(entries);
            Log.d(TAG, "DB update from LiveData in ViewModel");
        });
    }


    /* TODO: Set the indicator visible before loading
             mLoadingIndicator.setVisibility(View.VISIBLE);

    public void onLoadFinished(Loader<String[]> loader, String[] weatherData) {

        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (weatherData != null) {
            showWeatherDataView();
            mForecastAdapter.setWeatherData(weatherData);
        } else {
            showErrorMessage();
        }
    }
    */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our menu layout to this menu */
        inflater.inflate(R.menu.forecast, menu);
        /* Return true so that the menu is displayed in the Toolbar */
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_refresh) {
            refreshData();
            return true;
        }
        else if (id == R.id.action_map){
            openMapLocation();
            return true;
        }
        else if (id == R.id.action_settings){
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    /*
     * When a preference changed set the flag true
     */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_units_key))) {
            // units have changed. update lists of weather entries accordingly
            UNIT_PREFERENCE_UPDATED = true;
        }
    }

    /* After deleting adapter data resyncs data from the net */
    private void refreshData(){
        mForecastAdapter.setWeatherData(null);
        // Resync the data
        SunshineSyncUtils.startImmediateSync(this.getApplicationContext());

    }
}