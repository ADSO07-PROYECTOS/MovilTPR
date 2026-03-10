package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

data class PlatoModel(
    @SerializedName("id")           val id: Int,
    @SerializedName("nombre")       val nombre: String,
    @SerializedName("descripcion")  val descripcion: String,
    @SerializedName("precio")       val precio: Double,
    @SerializedName("imagen")       val imagen: String?,
    @SerializedName("categoria_id") val categoriaId: Int
)
