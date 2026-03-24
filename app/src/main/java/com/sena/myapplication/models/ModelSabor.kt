package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para un sabor disponible.
 * Viene incluido en la respuesta de /api/extras.
 *
 * Principio SRP: Responsabilidad única de representar un sabor del backend.
 * Los campos @SerializedName coinciden con las claves JSON de /api/extras.
 */
data class ModelSabor(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio") val precio: Double = 0.0
)

