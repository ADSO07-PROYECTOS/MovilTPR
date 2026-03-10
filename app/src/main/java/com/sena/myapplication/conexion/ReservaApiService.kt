package com.sena.myapplication.conexion

import com.sena.myapplication.models.ReservaRequest
import com.sena.myapplication.models.ReservaResponse
import com.sena.myapplication.Tematica
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ReservaApiService {

    @GET("api/tematicas")
    fun obtenerTematicas(): Call<List<Tematica>>

    @POST("api/reservas")
    fun crearReserva(@Body body: ReservaRequest): Call<ReservaResponse>
}
