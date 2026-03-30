package com.sena.myapplication.services

import android.util.Log
import com.sena.myapplication.models.ModelCategoria
import com.sena.myapplication.models.ModelExtras
import com.sena.myapplication.models.ModelPlato
import okhttp3.ConnectionPool
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okio.Buffer
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit


interface ConexionServiceMenu {

    @GET("api/categorias")
    suspend fun obtenerCategorias(): Response<List<ModelCategoria>>

    @GET("api/platos/{id_categoria}")
    suspend fun obtenerPlatosPorCategoria(
        @Path("id_categoria") idCategoria: Int
    ): Response<List<ModelPlato>>

    @GET("api/extras")
    suspend fun obtenerExtras(): Response<ModelExtras>

    companion object {

        const val BASE_URL = "http://54.156.114.70:62001//"

        const val BASE_URL_IMAGENES = "http://54.156.114.70:62001/static/img/"

        private val loggingInterceptor = Interceptor { chain ->
            val request = chain.request()
            val bodyStr = request.body()?.let { body ->
                val buffer = Buffer()
                body.writeTo(buffer)
                buffer.readUtf8()
            } ?: "null"
            Log.d("MenuAPI", ">>> ${request.method()} ${request.url()}")
            Log.d("MenuAPI", ">>> Body: $bodyStr")

            val response = chain.proceed(request)
            val responseBody = response.peekBody(Long.MAX_VALUE).string()
            Log.d("MenuAPI", "<<< ${response.code()} ${response.message()}")
            Log.d("MenuAPI", "<<< Body: $responseBody")

            response
        }


        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
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

        val instance: ConexionServiceMenu by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ConexionServiceMenu::class.java)
        }
    }
}

