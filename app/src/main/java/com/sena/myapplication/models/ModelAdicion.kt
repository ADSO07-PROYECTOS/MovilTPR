package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para una adición disponible (ej: ingrediente extra).
 * Viene incluida en la respuesta de /api/extras.
 */
data class ModelAdicion(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio") val precio: Double
)

