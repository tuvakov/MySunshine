package com.example.android.sunshine.utilities;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.sunshine.data.AppExecutors;
import com.example.android.sunshine.data.database.AppDatabase;
import com.example.android.sunshine.sync.SunshineSyncIntentService;

public class SunshineSyncUtils {

    private static final String TAG = SunshineSyncUtils.class.getSimpleName();
    private static boolean sInitialized = false;

    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     *
     * @param context Context that will be passed to other methods and used to access the
     *                ContentResolver
     */
    public static void initialize(@NonNull final Context context){
        /*
         * Only perform initialization once per app lifetime. If initialization has already been
         * performed, we have nothing to do in this method.
         */
        if (sInitialized){
            Log.d(TAG, "DB is already initialized");
            return;
        }

        /*
         * If DB is empty then sync
         * Otherwise no need
         */
        AppExecutors.getInstance().getDiskIO().execute(() -> {
            int count = AppDatabase.getsInstance(context).weatherDao().countAll();
            if (count == 0){
                startImmediateSync(context);
                sInitialized = true;
            }
        });

    }

    public static void startImmediateSync(Context context){
        Intent syncWeatherIntent = new Intent(context, SunshineSyncIntentService.class);
        context.startService(syncWeatherIntent);
    }
}
