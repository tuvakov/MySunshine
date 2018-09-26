package com.example.android.sunshine.data;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;


import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {
    // For singleton instantiation
    private static final Object LOCK = new Object();
    private static AppExecutors sInstance;
    private Executor diskIO;
    private Executor networkIO;
    private Executor mainThread;

    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread){
        this.diskIO = diskIO;
        this.networkIO = networkIO;
        this.mainThread = mainThread;
    }

    public static AppExecutors getInstance() {
        if (sInstance == null){
            synchronized (LOCK){
                sInstance = new AppExecutors(
                        // All of the DB operations will run in a single thread
                        Executors.newSingleThreadExecutor(),
                        // Three pooled threads for network operations
                        Executors.newFixedThreadPool(3),
                        new MainThreadExecutor());
            }
        }

        return sInstance;
    }

    // Getters
    public Executor getDiskIO() { return diskIO; }

    public Executor getNetworkIO() { return networkIO; }

    public Executor getMainThread() { return mainThread; }

    /* Implement MainThread Executor
     * This will help to put Runnable instances to the main thread's handler
     * Currently, we don't need this since we can use runOnUiThread() method
     */
    private static class MainThreadExecutor implements Executor{
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable runnable){
            mainThreadHandler.post(runnable);
        }
    }
}
