package com.sena.myapplication.conexion

import android.util.Log
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okio.Buffer
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ReservaRetrofitClient {

    private const val BASE_URL = "http://147.182.238.195:5005/"

    private val loggingInterceptor = Interceptor { chain ->
        val request = chain.request()
        val bodyStr = request.body()?.let { body ->
            val buffer = Buffer()
            body.writeTo(buffer)
            buffer.readUtf8()
        } ?: "null"
        Log.d("ReservaAPI", ">>> ${request.method()} ${request.url()}")
        Log.d("ReservaAPI", ">>> Body: $bodyStr")

        val response: Response = chain.proceed(request)
        val responseBody = response.peekBody(Long.MAX_VALUE).string()
        Log.d("ReservaAPI", "<<< ${response.code()} ${response.message()}")
        Log.d("ReservaAPI", "<<< Body: $responseBody")

        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
        .retryOnConnectionFailure(true)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor { chain ->
            chain.proceed(
                chain.request().newBuilder()
                    .header("Connection", "close")
                    .build()
            )
        }
        .build()

    val instance: ReservaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ReservaApiService::class.java)
    }
}
