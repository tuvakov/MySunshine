package com.example.android.sunshine.utilities;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.example.android.sunshine.data.AppExecutors;
import com.example.android.sunshine.data.database.AppDatabase;
import com.example.android.sunshine.sync.SunshineFirebaseJobService;
import com.example.android.sunshine.sync.SunshineSyncIntentService;
import com.firebase.jobdispatcher.Constraint;
import com.firebase.jobdispatcher.Driver;
import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.firebase.jobdispatcher.Lifetime;
import com.firebase.jobdispatcher.Trigger;

import java.util.concurrent.TimeUnit;

public class SunshineSyncUtils {
    private static final String TAG = SunshineSyncUtils.class.getSimpleName();
    /* Constants for FirebaseJobDispatcher */
    // Tag
    private static final String SUNSHINE_SYNC_TAG = "sunshine-sync-tag";
    // Interval at which to sync data
    private static final int REMINDER_INTERVAL_HOURS = 3;
    private static final int REMINDER_INTERVAL_SECONDS = (int) TimeUnit.HOURS.toSeconds(REMINDER_INTERVAL_HOURS);
//    private static final int REMINDER_INTERVAL_SECONDS = 60;
    private static final int SYNC_FLEXTIME_MINUTES = 5;
    private static final int SYNC_FLEXTIME_SECONDS = (int) TimeUnit.MINUTES.toSeconds(SYNC_FLEXTIME_MINUTES);
//    private static final int SYNC_FLEXTIME_SECONDS = 15;

    private static boolean sInitialized = false;


    /**
     * Creates periodic sync tasks and checks to see if an immediate sync is required. If an
     * immediate sync is required, this method will take care of making sure that sync occurs.
     *
     * @param context Context that will be passed to other methods and used to access the
     *                ContentResolver
     */
    synchronized public static void initialize(@NonNull final Context context){
        /*
         * Only perform initialization once per app lifetime. If initialization has already been
         * performed, we have nothing to do in this method.
         */
        if (sInitialized){
            Log.d(TAG, "DB is already initialized");
            return;
        }

        /* Schedule the job */
        sInitialized = true;
        scheduleFirebaseJobDispatcherSync(context);

        /*
         * If DB is empty then sync
         * Otherwise no need
         */
        AppExecutors.getInstance().getDiskIO().execute(() -> {
            int count = AppDatabase.getsInstance(context).weatherDao().countAll();
            if (count == 0){
                startImmediateSync(context);
            }
        });

    }

    public static void startImmediateSync(Context context){
        Intent syncWeatherIntent = new Intent(context, SunshineSyncIntentService.class);
        context.startService(syncWeatherIntent);
    }

    private static void scheduleFirebaseJobDispatcherSync(@NonNull final Context context){

        Driver driver = new GooglePlayDriver(context);
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(driver);

        /* Make a Job to periodically sync data */
        Job weatherSyncJob = dispatcher.newJobBuilder()
                /* The Service that will be used to sync Sunshine's data */
                .setService(SunshineFirebaseJobService.class)
                /* Set the UNIQUE tag used to identify this Job */
                .setTag(SUNSHINE_SYNC_TAG)
                /* Network constraints on which this Job should run. */
                .setConstraints(Constraint.ON_ANY_NETWORK)
                /*
                 * setLifetime sets how long this job should persist.
                 * The options are to keep the Job "forever" or to have it die the next time the device boots up.
                 */
                .setLifetime(Lifetime.FOREVER)
                /* The job should reoccur so that the weather will be up to date */
                .setRecurring(true)
                /*
                 * Weather data should be synced every 3 hours. The first argument for
                 * Trigger's static executionWindow method is the start of the time frame when the
                 * sync should be performed. The second argument is the latest point in time at
                 * which the data should be synced.
                 */
                .setTrigger(Trigger.executionWindow(
                        REMINDER_INTERVAL_SECONDS,
                        REMINDER_INTERVAL_SECONDS + SYNC_FLEXTIME_SECONDS))
                .setReplaceCurrent(true)
                .build();

        /* Schedule the Job with dispatcher */
        dispatcher.schedule(weatherSyncJob);
        Log.d(TAG, "Scheduled sync with FirebaseJobDispatcher");
    }
}
