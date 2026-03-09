
package com.sena.myapplication

import com.google.gson.annotations.SerializedName

data class Categoria(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tamano") val tamano: String,
    @SerializedName("imagen") val imagen: String
)
