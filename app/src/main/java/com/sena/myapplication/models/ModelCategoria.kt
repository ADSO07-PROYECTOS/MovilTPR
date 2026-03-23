package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para las categorías del menú.
 * Principio SRP: Responsabilidad única de representar una categoría del backend.
 * Los campos @SerializedName coinciden con las claves JSON de /api/categorias.
 */
data class ModelCategoria(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tamano") val tamano: String,
    @SerializedName("imagen") val imagen: String
)

