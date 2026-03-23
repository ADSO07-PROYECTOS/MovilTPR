package com.sena.myapplication.services

import android.util.Log
import com.sena.myapplication.models.ModelTematica
import com.sena.myapplication.models.ReservaRequest
import com.sena.myapplication.models.ReservaResponse
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import java.util.concurrent.TimeUnit

/**
 * Interfaz de conexión Retrofit para el API de reservas (puerto 5005).
 *
 * Principio ISP: Solo endpoints de reservas y temáticas.
 * Principio SRP: El companion object concentra la configuración de red
 * para este servicio específico.
 */
interface ConexionServiceReserva {

    /** Obtiene la lista de temáticas disponibles para reservar. */
    @GET("api/tematicas")
    suspend fun obtenerTematicas(): Response<List<ModelTematica>>

    /** Crea una nueva reserva con datos de cliente, reserva y pedido. */
    @POST("api/reservas")
    suspend fun crearReserva(@Body body: ReservaRequest): Response<ReservaResponse>


    companion object {

        /** URL base del servidor Flask de reservas. */
        const val BASE_URL = "http://147.182.238.195:5005/"

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
            Log.d("ReservaAPI", ">>> ${request.method()} ${request.url()}")
            Log.d("ReservaAPI", ">>> Body: $bodyStr")

            val response = chain.proceed(request)
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            Log.d("ReservaAPI", "<<< ${response.code()} ${response.message()}")
            Log.d("ReservaAPI", "<<< Body: $responseBody")

            response
        }

        /**
         * Cliente OkHttp con configuración anti-Flask:
         * pool vacío + Connection: close + logging interceptor.
         * Timeouts más amplios porque POST /api/reservas puede demorar.
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
        val instance: ConexionServiceReserva by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ConexionServiceReserva::class.java)
        }
    }
}

