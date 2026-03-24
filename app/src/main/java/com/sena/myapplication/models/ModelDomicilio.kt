package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

// ==================================================================
// Modelos de SOLICITUD (Request) — POST /api/domicilios (puerto 5004)
// ==================================================================

data class DomicilioRequest(
    @SerializedName("cliente")    val cliente: ClienteReserva,
    @SerializedName("domicilio")  val domicilio: DatosDomicilio,
    @SerializedName("productos")  val productos: List<PedidoItem>
)

data class DatosDomicilio(
    @SerializedName("direccion")        val direccion: String,
    @SerializedName("metodo_pago")      val metodoPago: String,
    @SerializedName("comprobante_pago") val comprobantePago: String? = null
)

// ==================================================================
// Modelo de RESPUESTA (Response)
// ==================================================================

typealias DomicilioResponse = ReservaResponse
