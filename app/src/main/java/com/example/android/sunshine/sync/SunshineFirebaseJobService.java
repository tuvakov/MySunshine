package com.example.android.sunshine.sync;

import android.os.AsyncTask;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;

public class SunshineFirebaseJobService extends JobService {
    private AsyncTask mBackgroundTask;
    @Override
    public boolean onStartJob(JobParameters job) {

        mBackgroundTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SunshineSyncTask.syncWeather(SunshineFirebaseJobService.this);
                Log.d("SunshineJobDispat", "Ran Scheduled sync");
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                super.onPostExecute(o);
                jobFinished(job,  false);
            }
        };

        mBackgroundTask.execute();
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
       if (mBackgroundTask != null) mBackgroundTask.cancel(true);
        return true;
    }
}
