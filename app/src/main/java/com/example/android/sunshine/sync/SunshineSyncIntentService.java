package com.example.android.sunshine.sync;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * IntentService class that handles sync operation in the background.
 */
public class SunshineSyncIntentService extends IntentService {
    public SunshineSyncIntentService() {
        super(SunshineSyncIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SunshineSyncTask.syncWeather(this);
    }
}
