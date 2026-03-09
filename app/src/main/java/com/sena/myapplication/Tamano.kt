
package com.sena.myapplication

import com.google.gson.annotations.SerializedName

data class Tamano(
    @SerializedName("id")             val id: Int,
    @SerializedName("nombre")         val nombre: String,
    @SerializedName("precio")         val precio: Double,
    @SerializedName("limite_sabores") val limiteSabores: Int
)
