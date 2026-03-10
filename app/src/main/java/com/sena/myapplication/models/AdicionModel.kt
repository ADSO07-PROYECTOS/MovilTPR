package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

data class AdicionModel(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio") val precio: Double
)
