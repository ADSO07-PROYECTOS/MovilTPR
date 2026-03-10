package com.sena.myapplication.conexion

import com.sena.myapplication.models.CategoriaModel
import com.sena.myapplication.models.ExtrasModel
import com.sena.myapplication.models.PlatoModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {

    @GET("api/categorias")
    fun obtenerCategorias(): Call<List<CategoriaModel>>

    @GET("api/platos/{id_categoria}")
    fun obtenerPlatosPorCategoria(
        @Path("id_categoria") idCategoria: Int
    ): Call<List<PlatoModel>>

    @GET("api/extras")
    fun obtenerExtras(): Call<ExtrasModel>
}
