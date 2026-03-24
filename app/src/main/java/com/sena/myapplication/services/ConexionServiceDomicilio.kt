package com.sena.myapplication.services

import android.util.Log
import com.sena.myapplication.models.DomicilioRequest
import com.sena.myapplication.models.DomicilioResponse
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

interface ConexionServiceDomicilio {

    @POST("api/domicilios")
    suspend fun crearDomicilio(@Body body: DomicilioRequest): Response<DomicilioResponse>

    companion object {

        const val BASE_URL = "http://147.182.238.195:5004/"


        private val loggingInterceptor = Interceptor { chain ->
            val request = chain.request()
            val bodyStr = request.body()?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } ?: "null"
            Log.d("DomicilioAPI", ">>> ${request.method()} ${request.url()}")
            Log.d("DomicilioAPI", ">>> Body: $bodyStr")

            val response = chain.proceed(request)
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            Log.d("DomicilioAPI", "<<< ${response.code()} ${response.message()}")
            Log.d("DomicilioAPI", "<<< Body: $responseBody")

            response
        }


        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
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
        }

        val instance: ConexionServiceDomicilio by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ConexionServiceDomicilio::class.java)
        }
    }
}

