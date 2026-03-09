
package com.sena.myapplication

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
