package com.rimaro.musify.network

import com.rimaro.musify.model.SpotifyToken
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface SpotifyAuthService {
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("token")
    suspend fun getToken(
        @Field("grant_type") grantType: String? = "authorization_code",
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Header("Authorization") authorization: String,
    ): SpotifyToken

    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("token")
    suspend fun refreshToken(
        @Field("grant_type") grantType: String? = "refresh_token",
        @Field("refresh_token") refreshToken: String,
        @Field("client_id") clientId: String,
        @Header("Authorization") authorization: String,
    ): SpotifyToken
}