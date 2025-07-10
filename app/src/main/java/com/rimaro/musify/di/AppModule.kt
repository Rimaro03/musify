package com.rimaro.musify.di

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Room
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.rimaro.musify.data.remote.retrofit.SpotifyApiService
import com.rimaro.musify.data.remote.retrofit.SpotifyAuthService
import com.rimaro.musify.domain.repository.SpotifyRepository
import com.rimaro.musify.utils.SpotifyTokenManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import javax.inject.Singleton
import com.rimaro.musify.di.Qualifiers.AuthRetrofit
import com.rimaro.musify.di.Qualifiers.ApiRetrofit
import com.rimaro.musify.data.local.db.AppDatabase
import com.rimaro.musify.ui.PlaybackManager
import com.rimaro.musify.utils.NewPipeHelper

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    val json = Json {
        ignoreUnknownKeys = true
    }
    // retrofit and api services
    @AuthRetrofit
    @Provides
    @Singleton
    fun provideAuthRetrofit(): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl("https://accounts.spotify.com/api/")
            .build()

    @ApiRetrofit
    @Provides
    @Singleton
    fun provideApiRetrofit(): Retrofit =
        Retrofit.Builder()
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .baseUrl("https://api.spotify.com/v1/")
            .build()

    @Provides
    @Singleton
    fun provideAuthApiService(@AuthRetrofit retrofit: Retrofit): SpotifyAuthService =
        retrofit.create(SpotifyAuthService::class.java)

    @Provides
    @Singleton
    fun provideApiService(@ApiRetrofit retrofit: Retrofit): SpotifyApiService =
        retrofit.create(SpotifyApiService::class.java)

    // shared preferences
    @Provides
    @Singleton
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
    }

    // spotify token manager
    @Provides
    @Singleton
    fun providesSpotifyTokenManager(
        spotifyRepository: SpotifyRepository,
        prefs: SharedPreferences
    ): SpotifyTokenManager {
        return SpotifyTokenManager(spotifyRepository, prefs)
    }

    // NewPipeHelper
    @Provides
    @Singleton
    fun provideNewPipeHelper() : NewPipeHelper = NewPipeHelper()

    // Room database
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ) : AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAudioStreamUrlDao(db: AppDatabase) = db.audioStreamUrlDao()

    @Provides
    @Singleton
    fun providePlaybackManager(): PlaybackManager {
        return PlaybackManager()
    }
}