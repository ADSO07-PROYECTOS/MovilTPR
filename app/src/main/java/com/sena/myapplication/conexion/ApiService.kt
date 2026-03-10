package com.sena.myapplication.conexion

import com.sena.myapplication.Categoria
import com.sena.myapplication.Extras
import com.sena.myapplication.Plato
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("api/categorias")
    fun obtenerCategorias(): Call<List<Categoria>>

    @GET("api/platos/{id_categoria}")
    fun obtenerPlatosPorCategoria(
        @Path("id_categoria") idCategoria: Int
    ): Call<List<Plato>>

    @GET("api/extras")
    fun obtenerExtras(): Call<Extras>
}
