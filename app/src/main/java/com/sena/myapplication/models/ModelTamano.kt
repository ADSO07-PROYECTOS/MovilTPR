package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para un tamaño de plato (ej: Personal, Mediana, Familiar).
 * Incluye el límite de sabores permitido por tamaño.
 *
 * Principio SRP: Responsabilidad única de representar un tamaño del backend.
 * Los campos @SerializedName coinciden con las claves JSON de /api/extras.
 */
data class ModelTamano(
    @SerializedName("id")             val id: Int,
    @SerializedName("nombre")         val nombre: String,
    @SerializedName("precio")         val precio: Double,
    @SerializedName("limite_sabores") val limiteSabores: Int
)

