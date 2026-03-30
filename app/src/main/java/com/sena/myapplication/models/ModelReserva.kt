package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName


data class ReservaRequest(
    @SerializedName("cliente") val cliente: ClienteReserva,
    @SerializedName("reserva") val reserva: DatosReserva,
    @SerializedName("pedido")  val pedido: List<PedidoItem>
)

data class ClienteReserva(
    @SerializedName("doc")    val doc: String,
    @SerializedName("nom")    val nom: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("tel")    val tel: String
)

data class DatosReserva(
    @SerializedName("fec")              val fec: String,
    @SerializedName("hor")              val hor: String,
    @SerializedName("tematica")         val tematica: Int,
    @SerializedName("personas")         val personas: Int,
    @SerializedName("piso")             val piso: Int,
    @SerializedName("metodo_pago")      val metodoPago: String,
    @SerializedName("comprobante_pago") val comprobantePago: String? = null
)

data class PedidoItem(
    @SerializedName("id")       val id: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio")   val precio: Double
)


data class ReservaResponse(
    @SerializedName("status")  val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("qr")      val qr: String?,
    @SerializedName("id")      val id: Int?
)

