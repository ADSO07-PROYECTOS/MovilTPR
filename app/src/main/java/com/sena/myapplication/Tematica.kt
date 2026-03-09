
package com.sena.myapplication

import com.google.gson.annotations.SerializedName

data class Tematica(
    @SerializedName("tematica_id")     val id: Int,
    @SerializedName("nombre_tematica") val nombre: String
)
