package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName
import com.sena.myapplication.models.AdicionModel

data class ExtrasModel(
  @SerializedName("tamanos")   val tamanoModels:   List<TamanoModel>,
  @SerializedName("adiciones") val adiciones: List<AdicionModel>,
  @SerializedName("sabores")   val sabores:   List<SaborModel>
)
