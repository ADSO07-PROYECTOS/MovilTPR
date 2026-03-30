package com.sena.myapplication.models

import com.google.gson.annotations.SerializedName

data class MisReservasResponse(
    @SerializedName("reservas")       val reservas: List<ModelMiReserva> = emptyList(),
    @SerializedName("sin_resultados") val sinResultados: Boolean = true
)

data class ModelMiReserva(
    @SerializedName("reserva_id")        val reservaId: Int = 0,
    @SerializedName("fecha_hora")        val fechaHora: String = "",
    @SerializedName("fecha_formato")     val fechaFormato: String = "",
    @SerializedName("cantidad_personas") val personas: Int = 0,
    @SerializedName("nombre_tematica")   val tematica: String = "",
    @SerializedName("tematica_id")       val tematicaId: Int = 0,
    @SerializedName("piso")              val piso: Int = 1,
    @SerializedName("estado")            val estado: String? = null,
    @SerializedName("qr_b64")            val qr: String? = null,
    @SerializedName("nombre")            val nombreCliente: String? = null,
    @SerializedName("email")             val email: String? = null,
    @SerializedName("telefono")          val telefono: String? = null,
    @SerializedName("cc_cliente")        val cedula: String? = null,
    @SerializedName("pedido")            val pedido: List<PedidoDetalle>? = null
) {

    fun extraerFecha(): String =
        if (fechaHora.length >= 10) fechaHora.substring(0, 10) else fechaHora

    fun extraerHora(): String {
        if (fechaHora.length >= 13) {
            val h = fechaHora.substring(11, 13).trimStart('0')
            return if (h.isEmpty()) "0" else h
        }
        return "0"
    }
}

data class PedidoDetalle(
    @SerializedName("nombre_producto") val nombre: String = "",
    @SerializedName("cantidad")        val cantidad: Int = 0,
    @SerializedName("notas")           val notas: String? = null
)

data class ActualizarReservaRequest(
    @SerializedName("fecha_hora") val fechaHora: String,
    @SerializedName("personas")   val personas: Int,
    @SerializedName("tematica")   val tematicaId: Int,
    @SerializedName("nombre")     val nombre: String? = null,
    @SerializedName("email")      val email: String? = null,
    @SerializedName("telefono")   val telefono: String? = null
)
