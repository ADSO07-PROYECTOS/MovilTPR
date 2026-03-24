package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

data class ModelSabor(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String
)

