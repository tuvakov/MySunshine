package com.example.android.sunshine;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.sunshine.data.AppExecutors;
import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.database.AppDatabase;
import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;
import com.example.android.sunshine.utilities.SunshineDateUtils;

import java.net.URL;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    /* List that holds weather data from db */
    private LiveData<List<WeatherEntry>> weatherEntries;
    private Application mApplication;
    private final String TAG = this.getClass().getSimpleName();

    public MainViewModel(@NonNull Application application) {
        super(application);
        mApplication = application;

        /* TODO: Currently networking is done here. It should be moved to Services later*/
        /* TODO: Insertion to fetched data DB should also be migrated to a Service. */
        /* TODO: The mock URL should be changed and geniune OWM apis should be used */

        /* TODO: Following operations are temporary. Should be updated later */
        /* Fetch weather data from the net and insert to DB */
        fetchAndInsertData();

        /* Get local time and convert UTC */
        long time = System.currentTimeMillis();
        time = SunshineDateUtils.getUTCDateFromLocal(time);
        long normalizedDate = SunshineDateUtils.normalizeDate(time);
        /* Read the inserted data from db */
        weatherEntries = AppDatabase.getsInstance(mApplication.getBaseContext()).weatherDao()
                .getWeatherForecasts(normalizedDate);
        Log.d(TAG, "Loaded data from the DB");

    }

    public LiveData<List<WeatherEntry>> getWeatherEntries() { return weatherEntries; }

    private WeatherEntry[] fetchWeatherData() {
        // Get the location first
        String location = SunshinePreferences.getPreferredWeatherLocation(mApplication.getBaseContext());

        // Make URL
        URL weatherRequestUrl = NetworkUtils.buildUrl(location);

        try {

            // Fetch data
            String jsonWeatherResponse = NetworkUtils
                    .getResponseFromHttpUrl(weatherRequestUrl);

            // Parse fetched JSON and get a WeatherEntry array
            WeatherEntry[] weatherEntries = OpenWeatherJsonUtils
                    .getFullWeatherStringsFromJson(mApplication.getBaseContext(), jsonWeatherResponse);

            if (weatherEntries == null)
                Log.d(TAG, "WeatherEntry[] is null");
            else
                Log.d(TAG, "WeatherEntry[] length " + weatherEntries.length);

            return weatherEntries;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /** TODO: Fetching part will be migrated.
     *  TODO: Fix offline situation.
     * */
    private void fetchAndInsertData(){

        AppExecutors.getInstance().getDiskIO().execute(
                () -> {
                    WeatherEntry[] weatherEntriesArray = fetchWeatherData();
                    // Insert the fetched data to the DB
                    if (weatherEntriesArray != null) {
                        Log.d(TAG, "Fetched data from the net");
                        AppDatabase.getsInstance(mApplication.getBaseContext()).weatherDao()
                                .bulkInsert(weatherEntriesArray);
                        Log.d(TAG, "Inserted data to the DB");
                    }
                }
        );
    }
}
