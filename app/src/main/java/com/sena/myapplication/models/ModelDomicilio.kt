package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

// ==================================================================
// Modelos de SOLICITUD (Request) — POST /api/domicilios (puerto 5004)
// ==================================================================

/**
 * Cuerpo completo del POST /api/domicilios.
 *
 * Las claves JSON deben coincidir exactamente con lo que espera el
 * microservicio Flask: "cliente", "domicilio" y "productos".
 */
data class DomicilioRequest(
    @SerializedName("cliente")    val cliente: ClienteReserva,
    @SerializedName("domicilio")  val domicilio: DatosDomicilio,
    @SerializedName("productos")  val productos: List<PedidoItem>
)

/**
 * Datos del domicilio: dirección de entrega y método de pago.
 *
 * Nota: el backend solo almacena "direccion" como campo único.
 * Si el usuario ingresa barrio/referencia, se debe concatenar
 * al campo [direccion] antes de construir este objeto.
 */
data class DatosDomicilio(
    @SerializedName("direccion")        val direccion: String,
    @SerializedName("metodo_pago")      val metodoPago: String,
    @SerializedName("comprobante_pago") val comprobantePago: String? = null
)

// ==================================================================
// Modelo de RESPUESTA (Response)
// ==================================================================

/**
 * Respuesta del servidor tras crear el domicilio.
 * Estructura idéntica a [ReservaResponse]: status, message, qr, id.
 */
typealias DomicilioResponse = ReservaResponse
