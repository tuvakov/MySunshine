package com.example.android.sunshine.utilities;
import android.content.Context;
import android.content.Intent;

import com.example.android.sunshine.sync.SunshineSyncIntentService;

public class SunshineSyncUtils {

    public static void startImmediateSync(Context context){
        Intent syncWeatherIntent = new Intent(context, SunshineSyncIntentService.class);
        context.startService(syncWeatherIntent);
    }
}
