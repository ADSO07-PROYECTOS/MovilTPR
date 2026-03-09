
package com.sena.myapplication

import com.google.gson.annotations.SerializedName

data class Sabor(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String
)
