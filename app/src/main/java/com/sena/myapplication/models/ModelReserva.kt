package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

// ==================================================================
// Modelos de SOLICITUD (Request) — POST /api/reservas
// ==================================================================

/**
 * Cuerpo completo del POST /api/reservas.
 * Principio SRP: Cada sub-modelo encapsula un concepto del dominio.
 */
data class ReservaRequest(
    @SerializedName("cliente") val cliente: ClienteReserva,
    @SerializedName("reserva") val reserva: DatosReserva,
    @SerializedName("pedido")  val pedido: List<PedidoItem>
)

/** Datos personales del cliente que realiza la reserva. */
data class ClienteReserva(
    @SerializedName("doc")    val doc: String,
    @SerializedName("nom")    val nom: String,
    @SerializedName("correo") val correo: String,
    @SerializedName("tel")    val tel: String
)

/** Datos específicos de la reserva (fecha, hora, piso, etc.). */
data class DatosReserva(
    @SerializedName("fec")              val fec: String,
    @SerializedName("hor")              val hor: String,
    @SerializedName("tematica")         val tematica: Int,
    @SerializedName("personas")         val personas: Int,
    @SerializedName("piso")             val piso: Int,
    @SerializedName("metodo_pago")      val metodoPago: String,
    @SerializedName("comprobante_pago") val comprobantePago: String? = null
)

/** Item individual del pedido asociado a la reserva. */
data class PedidoItem(
    @SerializedName("id")       val id: Int,
    @SerializedName("cantidad") val cantidad: Int,
    @SerializedName("precio")   val precio: Double
)

// ==================================================================
// Modelo de RESPUESTA (Response)
// ==================================================================

/**
 * Respuesta del servidor tras crear la reserva.
 * El campo [qr] contiene la imagen QR codificada en Base64.
 */
data class ReservaResponse(
    @SerializedName("status")  val status: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("qr")      val qr: String?,
    @SerializedName("id")      val id: Int?
)

