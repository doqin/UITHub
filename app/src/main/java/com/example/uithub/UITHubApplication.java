package com.example.uithub;

import android.app.Application;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class UITHubApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        setupAnnouncementWorker();
    }

    private void setupAnnouncementWorker() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(
                AnnouncementWorker.class,
                1, TimeUnit.HOURS // check every 1 hour
        ).build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "AnnouncementCheckWork",
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
        );
    }
}
