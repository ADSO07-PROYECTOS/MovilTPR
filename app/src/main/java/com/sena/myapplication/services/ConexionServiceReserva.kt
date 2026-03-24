package com.sena.myapplication.services

import android.util.Log
import com.sena.myapplication.models.ActualizarReservaRequest
import com.sena.myapplication.models.ModelMiReserva
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
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import java.util.concurrent.TimeUnit


interface ConexionServiceReserva {

    @GET("api/tematicas")
    suspend fun obtenerTematicas(): Response<List<ModelTematica>>

    @POST("api/reservas")
    suspend fun crearReserva(@Body body: ReservaRequest): Response<ReservaResponse>

    @GET("api/reservas/{cedula}")
    suspend fun buscarReservasPorCedula(
        @Path("cedula") cedula: String
    ): Response<List<ModelMiReserva>>

    @PUT("api/reservas/{reserva_id}")
    suspend fun actualizarReserva(
        @Path("reserva_id") reservaId: Int,
        @Body body: ActualizarReservaRequest
    ): Response<ReservaResponse>

    @DELETE("api/reservas/{reserva_id}")
    suspend fun eliminarReserva(
        @Path("reserva_id") reservaId: Int
    ): Response<ReservaResponse>


    companion object {

        const val BASE_URL = "http://147.182.238.195:5005/"


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

