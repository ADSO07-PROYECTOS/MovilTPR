package com.sena.myapplication.services

import android.util.Log
import com.sena.myapplication.models.ActualizarReservaRequest
import com.sena.myapplication.models.MisReservasResponse
import com.sena.myapplication.models.ModelTematica
import com.sena.myapplication.models.ReservaResponse
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

/**
 * Interfaz de conexión Retrofit para el microservicio de "Mis Reservas" (puerto 5007).
 *
 * Principio ISP: Solo endpoints de consulta, edición y eliminación de reservas.
 * Principio SRP: El companion object concentra la configuración de red
 * para este servicio específico.
 */
interface ConexionServiceMisReservas {

    /**
     * Consulta las reservas de un cliente por su cédula.
     * Retorna un wrapper con la lista de reservas y un flag sin_resultados.
     */
    @GET("api/mis_reservas")
    suspend fun buscarReservasPorCedula(
        @Query("cedula") cedula: String
    ): Response<MisReservasResponse>

    /** Actualiza una reserva existente. */
    @PUT("api/mis_reservas/{id_reserva}")
    suspend fun actualizarReserva(
        @Path("id_reserva") reservaId: Int,
        @Body body: ActualizarReservaRequest
    ): Response<ReservaResponse>

    /** Elimina una reserva existente. */
    @DELETE("api/mis_reservas/{id_reserva}")
    suspend fun eliminarReserva(
        @Path("id_reserva") reservaId: Int
    ): Response<ReservaResponse>

    /** Obtiene la lista de temáticas para el selector de edición. */
    @GET("api/tematicas")
    suspend fun obtenerTematicas(): Response<List<ModelTematica>>


    companion object {

        /** URL base del microservicio Flask de mis reservas. */
        const val BASE_URL = "http://147.182.238.195:5007/"

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
            Log.d("MisReservasAPI", ">>> ${request.method()} ${request.url()}")
            Log.d("MisReservasAPI", ">>> Body: $bodyStr")

            val response = chain.proceed(request)
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            Log.d("MisReservasAPI", "<<< ${response.code()} ${response.message()}")
            Log.d("MisReservasAPI", "<<< Body: $responseBody")

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
        val instance: ConexionServiceMisReservas by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ConexionServiceMisReservas::class.java)
        }
    }
}

