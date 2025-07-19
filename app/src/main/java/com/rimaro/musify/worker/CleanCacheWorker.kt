package com.rimaro.musify.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.rimaro.musify.domain.repository.SpotifyRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.lang.Exception

@HiltWorker
class CleanCacheWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val spotifyRepository: SpotifyRepository
) : CoroutineWorker (appContext, workerParams){
    override suspend fun doWork(): Result {
        try {
            spotifyRepository.cleanCache()
            return Result.success()
        } catch (_: Exception) {
            return Result.failure()
        }
    }
}