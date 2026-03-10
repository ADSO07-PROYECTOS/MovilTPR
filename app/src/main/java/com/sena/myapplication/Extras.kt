
package com.sena.myapplication

import com.google.gson.annotations.SerializedName
import com.sena.myapplication.models.TamanoModel

data class Extras(
  @SerializedName("tamanos")   val tamanoModels:   List<TamanoModel>,
  @SerializedName("adiciones") val adiciones: List<Adicion>,
  @SerializedName("sabores")   val sabores:   List<Sabor>
)
