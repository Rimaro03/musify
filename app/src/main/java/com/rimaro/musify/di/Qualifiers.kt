package com.rimaro.musify.di

import jakarta.inject.Qualifier

object Qualifiers {
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AuthRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ApiRetrofit
}