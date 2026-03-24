package com.sena.myapplication.models

/**
 * Modelo local para representar un item en el carrito de compras.
 * No se serializa al backend; solo se usa para transporte entre Activities vía Intent extras.
 *
 * Principio SRP: Responsabilidad única de representar un item del carrito.
 * Principio ISP: Modelo ligero que no arrastra campos innecesarios del backend.
 */
data class ModelCarritoItem(
    val nombrePlato: String,
    val nombreTamano: String,
    val cantidad: Int,
    val precioUnitario: Double,
    val precioTotal: Double
)

