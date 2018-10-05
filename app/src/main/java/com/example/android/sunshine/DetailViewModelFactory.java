package com.example.android.sunshine;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.example.android.sunshine.data.database.AppDatabase;

public class DetailViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    final private long mWeatherEntryDate;
    final private AppDatabase mDb;

    public DetailViewModelFactory(long weatherEntryDate, AppDatabase db){
        mWeatherEntryDate = weatherEntryDate;
        mDb = db;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass){
        return (T) new DetailViewModel(mDb, mWeatherEntryDate);
    }
}
