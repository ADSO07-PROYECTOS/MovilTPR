package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

data class CategoriaModel(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tamano") val tamano: String,
    @SerializedName("imagen") val imagen: String
)
