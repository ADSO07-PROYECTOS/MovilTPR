package com.sena.myapplication

import com.google.gson.annotations.SerializedName

data class Adicion(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("precio") val precio: Double
)
