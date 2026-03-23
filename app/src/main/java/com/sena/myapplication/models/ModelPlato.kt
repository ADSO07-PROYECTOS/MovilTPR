package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para un plato del menú.
 * Principio SRP: Solo representa un plato tal como lo devuelve /api/platos/{id}.
 */
data class ModelPlato(
    @SerializedName("id")           val id: Int,
    @SerializedName("nombre")       val nombre: String,
    @SerializedName("descripcion")  val descripcion: String,
    @SerializedName("precio")       val precio: Double,
    @SerializedName("imagen")       val imagen: String?,
    @SerializedName("categoria_id") val categoriaId: Int
)

