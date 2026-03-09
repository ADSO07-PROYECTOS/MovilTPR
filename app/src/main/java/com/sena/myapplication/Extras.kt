
package com.sena.myapplication

import com.google.gson.annotations.SerializedName

data class Extras(
    @SerializedName("tamanos")   val tamanos:   List<Tamano>,
    @SerializedName("adiciones") val adiciones: List<Adicion>,
    @SerializedName("sabores")   val sabores:   List<Sabor>
)
