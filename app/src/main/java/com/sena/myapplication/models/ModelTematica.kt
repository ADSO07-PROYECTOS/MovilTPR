package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para una temática de reserva.
 * Obtenida desde /api/tematicas (puerto 5005).
 *
 * Principio SRP: Responsabilidad única de representar una temática del backend.
 * Los campos @SerializedName coinciden con las claves JSON del API Flask.
 */
data class ModelTematica(
    @SerializedName("tematica_id")     val id: Int,
    @SerializedName("nombre_tematica") val nombre: String
)

