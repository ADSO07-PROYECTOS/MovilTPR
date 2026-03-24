package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

data class ModelTematica(
    @SerializedName("tematica_id")     val id: Int,
    @SerializedName("nombre_tematica") val nombre: String
)

