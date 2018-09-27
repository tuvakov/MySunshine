package com.example.android.sunshine;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.android.sunshine.data.database.AppDatabase;
import com.example.android.sunshine.data.database.WeatherEntry;

public class DetailViewModel extends ViewModel {

    private LiveData<WeatherEntry> mWeatherEntry;
    private final String TAG = this.getClass().getSimpleName();

    public DetailViewModel(AppDatabase db, int weatherEntryId){
        mWeatherEntry = db.weatherDao().getWeatherById(weatherEntryId);
        Log.d(TAG, "Retrieve data from DB for a single day");
    }

    public LiveData<WeatherEntry> getWeatherEntry(){ return mWeatherEntry; }
}
