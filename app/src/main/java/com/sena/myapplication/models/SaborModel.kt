package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

data class SaborModel(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String
)
