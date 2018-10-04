package com.example.android.sunshine.sync;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateUtils;
import android.util.Log;

import com.example.android.sunshine.data.SunshinePreferences;
import com.example.android.sunshine.data.database.AppDatabase;
import com.example.android.sunshine.data.database.WeatherEntry;
import com.example.android.sunshine.utilities.NetworkUtils;
import com.example.android.sunshine.utilities.NotificationUtils;
import com.example.android.sunshine.utilities.OpenWeatherJsonUtils;

import java.net.URL;

/**
 * Class that contains "sync" logic of the app.
 */

public class SunshineSyncTask {

    private static final String TAG = SunshineSyncTask.class.getSimpleName();

    /**
     * Method that is called from the SyncIntentService
     * Basically, it calls a private method that fetches data from the net and inserts it to the DB
     * If the notification constraints match, then it shows notification
     * @param context
     */
    public static void syncWeather(Context context){
        fetchAndInsertData(context);

        // Save the synced time
        SunshinePreferences.saveLastSyncTime(context, System.currentTimeMillis());
        Log.d(TAG, "Saved the last sync time");

        // Check if notifications are enables
        boolean areEnabled = SunshinePreferences.areNotificationsEnabled(context);

        /* Check if a day has passed since the last notification */
        long elapsedTime = SunshinePreferences.getEllapsedTimeSinceLastNotification(context);
        boolean hasDayPassed = elapsedTime >= DateUtils.DAY_IN_MILLIS;

        /* If both conditions met then show a notification */
        if (areEnabled && hasDayPassed)
            NotificationUtils.notifyUserOfNewWeather(context);
    }

    /**
     * Helper method that fetches data and parses JSON
     * @param context
     * @return
     */
    private static WeatherEntry[] fetchWeatherData(Context context) {
        // Get the location first
        String location = SunshinePreferences.getPreferredWeatherLocation(context);

        // Make URL
        URL weatherRequestUrl = NetworkUtils.buildUrl(location);

        try {
            // Fetch data
            String jsonWeatherResponse = NetworkUtils
                    .getResponseFromHttpUrl(weatherRequestUrl);

            /* Check if network connection was successful */
            if (jsonWeatherResponse == null)
                return null;

            // Parse fetched JSON and get a WeatherEntry array
            WeatherEntry[] weatherEntries = OpenWeatherJsonUtils
                    .getFullWeatherStringsFromJson(context, jsonWeatherResponse);

            return weatherEntries;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Helper method that inserts fetched data to DB
     * @param context
     */
    private static void fetchAndInsertData(Context context){

        // Fetch the data
        WeatherEntry[] weatherEntriesArray = fetchWeatherData(context);
        // Insert the fetched data to the DB
        if (weatherEntriesArray != null) {
            Log.d(TAG, "Fetched data from the net");
            // Delete old data
            long today = weatherEntriesArray[0].getDate();
            AppDatabase.getsInstance(context).weatherDao().deleteOldWeather(today);

            // Insert the new data
            AppDatabase.getsInstance(context).weatherDao().bulkInsert(weatherEntriesArray);
            Log.d(TAG, "Inserted data to the DB");
        }
        else {
            Log.d(TAG, "WeatherEntry[] was null");
        }
    }
}
