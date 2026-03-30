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

interface ConexionServiceMisReservas {
    @GET("api/mis_reservas")
    suspend fun buscarReservasPorCedula(
        @Query("cedula") cedula: String
    ): Response<MisReservasResponse>

    @PUT("api/mis_reservas/{id_reserva}")
    suspend fun actualizarReserva(
        @Path("id_reserva") reservaId: Int,
        @Body body: ActualizarReservaRequest
    ): Response<ReservaResponse>

    @DELETE("api/mis_reservas/{id_reserva}")
    suspend fun eliminarReserva(
        @Path("id_reserva") reservaId: Int
    ): Response<ReservaResponse>

    @GET("api/tematicas")
    suspend fun obtenerTematicas(): Response<List<ModelTematica>>


    companion object {

        const val BASE_URL = "http://54.156.114.70:62001/"


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

