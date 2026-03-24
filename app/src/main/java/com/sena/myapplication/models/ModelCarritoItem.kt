package com.sena.myapplication.models

data class ModelCarritoItem(
    val nombrePlato: String,
    val nombreTamano: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val precioTotal: Double
)

