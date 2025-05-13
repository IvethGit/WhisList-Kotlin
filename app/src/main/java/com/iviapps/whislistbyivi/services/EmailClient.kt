package com.iviapps.whislistbyivi.services

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object EmailClient {
    private const val BASE_URL = "https://api.brevo.com/v3/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Muestra cuerpo, headers, etc.
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    val service: BrevoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
            .create(BrevoApi::class.java)
    }
}
