package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

data class ModelExtras(
    @SerializedName("tamanos")   val tamanos:   List<ModelTamano>,
    @SerializedName("adiciones") val adiciones: List<ModelAdicion>,
    @SerializedName("sabores")   val sabores:   List<ModelSabor>
)

