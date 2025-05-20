package com.example.lw4_3.domain

import android.app.Application
import com.example.lw4_3.domain.data.RegisterRequest
import com.example.lw4_3.domain.data.RegisterResponse
import com.example.lw4_3.domain.data.UserResponse
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Body
import java.io.File
import java.util.concurrent.TimeUnit

interface UserApiService {
    @POST("users/new")
    suspend fun registerUser(@Body request: RegisterRequest): RegisterResponse

    @GET("user")
    suspend fun getUser(@Query("username") username: String): UserResponse

    companion object {
        private const val BASE_URL = "http://10.0.2.2:5000/" // Для эмулятора

        fun create(application : Application): UserApiService {
            val cacheDir = File(application.cacheDir, "cache")
            val client = OkHttpClient.Builder()
                .cache(Cache(directory = cacheDir, 10 * 1024 * 1024)) // 10MB кэш
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                })
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(UserApiService::class.java)
        }
    }
}