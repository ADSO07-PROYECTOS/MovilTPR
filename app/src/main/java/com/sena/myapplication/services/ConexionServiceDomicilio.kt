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

/**
 * Interfaz de conexión Retrofit para el API de domicilios (puerto 5004).
 *
 * Microservicio Flask separado que gestiona pedidos a domicilio.
 * Principio ISP: Solo el endpoint de domicilios.
 * Principio SRP: El companion object concentra la configuración de red
 * exclusiva para este microservicio.
 */
interface ConexionServiceDomicilio {

    /** Crea un pedido a domicilio con datos de cliente, domicilio y productos. */
    @POST("api/domicilios")
    suspend fun crearDomicilio(@Body body: DomicilioRequest): Response<DomicilioResponse>

    companion object {

        /** URL base del microservicio Flask de domicilios. */
        const val BASE_URL = "http://147.182.238.195:5004/"

        /**
         * Interceptor de logging para depuración.
         * Registra método, URL, body de request y response en Logcat.
         */
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

        /**
         * Cliente OkHttp con configuración anti-Flask:
         * pool vacío + Connection: close + logging interceptor.
         */
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

        /** Instancia singleton de la interfaz Retrofit lista para usar. */
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

