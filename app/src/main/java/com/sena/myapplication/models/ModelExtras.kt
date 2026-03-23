package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

/**
 * Modelo contenedor de la respuesta de /api/extras.
 * Agrupa tamaños, adiciones y sabores en un solo objeto.
 * Principio ISP: Los consumidores solo acceden a la sublista que necesitan.
 */
data class ModelExtras(
    @SerializedName("tamanos")   val tamanos:   List<ModelTamano>,
    @SerializedName("adiciones") val adiciones: List<ModelAdicion>,
    @SerializedName("sabores")   val sabores:   List<ModelSabor>
)

