package com.rimaro.musify

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import com.google.android.material.color.DynamicColors
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.rimaro.musify.worker.CleanCacheWorker
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class MusifyApplication: Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        val cleanCacheRequest = PeriodicWorkRequestBuilder<CleanCacheWorker>(1, TimeUnit.DAYS)
            .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "cache cleanup",
                ExistingPeriodicWorkPolicy.REPLACE,
                cleanCacheRequest
            )
    }
}