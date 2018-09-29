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

/**
 * ViewModel class for main activity
 * The constructor reads data from db and caches it weatherEntries LiveData object.
 * The LiveData object can be obtained by its getter method from the MainActivity
 */
public class MainViewModel extends AndroidViewModel {

    /* LiveData that holds weather data from db */
    private LiveData<List<WeatherEntry>> weatherEntries;
    private final String TAG = this.getClass().getSimpleName();

    public MainViewModel(@NonNull Application application) {
        super(application);

        /* Get local time and convert UTC */
        long time = SunshineDateUtils.getUTCDateFromLocal(System.currentTimeMillis());
        long normalizedDate = SunshineDateUtils.normalizeDate(time);
        /* Read data from db */
        weatherEntries = AppDatabase.getsInstance(application.getBaseContext()).weatherDao()
                .getWeatherForecasts(normalizedDate);
        Log.d(TAG, "Loaded data from the DB");
    }

    public LiveData<List<WeatherEntry>> getWeatherEntries() { return weatherEntries; }

}
