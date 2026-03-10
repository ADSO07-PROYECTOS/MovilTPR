package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

data class TematicaModel(
    @SerializedName("tematica_id")     val id: Int,
    @SerializedName("nombre_tematica") val nombre: String
)
