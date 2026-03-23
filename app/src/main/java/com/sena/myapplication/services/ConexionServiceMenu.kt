package com.sena.myapplication.services

import com.sena.myapplication.models.ModelCategoria
import com.sena.myapplication.models.ModelExtras
import com.sena.myapplication.models.ModelPlato
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

/**
 * Interfaz de conexión Retrofit para el API de menú (puerto 5001).
 *
 * Principio ISP (Interface Segregation): Esta interfaz solo expone
 * los endpoints relacionados con el menú del restaurante.
 *
 * Principio OCP (Open/Closed): Para agregar nuevos endpoints de menú,
 * solo se añaden funciones suspend aquí sin modificar las existentes.
 *
 * La URL base y la instancia de Retrofit se definen en el companion object
 * siguiendo la convención del instructor Diego Pinilla.
 */
interface ConexionServiceMenu {

    /** Obtiene la lista de categorías del menú. */
    @GET("api/categorias")
    suspend fun obtenerCategorias(): Response<List<ModelCategoria>>

    /** Obtiene los platos de una categoría específica. */
    @GET("api/platos/{id_categoria}")
    suspend fun obtenerPlatosPorCategoria(
        @Path("id_categoria") idCategoria: Int
    ): Response<List<ModelPlato>>

    /** Obtiene los extras (tamaños, sabores y adiciones) disponibles. */
    @GET("api/extras")
    suspend fun obtenerExtras(): Response<ModelExtras>

    companion object {

        /** URL base del servidor Flask de menú. */
        const val BASE_URL = "http://147.182.238.195:5001/"

        /** URL base para imágenes estáticas (puerto 5000). */
        const val BASE_URL_IMAGENES = "http://147.182.238.195:5000/static/img/"

        /**
         * Cliente OkHttp configurado para Flask:
         * - ConnectionPool vacío + header "Connection: close" para evitar
         *   errores de "unexpected end of stream" con Flask.
         * - Reintentos automáticos habilitados a nivel de transporte.
         */
        private val okHttpClient: OkHttpClient by lazy {
            OkHttpClient.Builder()
                .connectionPool(ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                .retryOnConnectionFailure(true)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
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

