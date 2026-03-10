package com.sena.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log
import com.sena.myapplication.conexion.RetrofitClient
import com.sena.myapplication.models.PlatoModel

class RepositoryPlatos {
    private val api = RetrofitClient.instance

    fun obtenerPlatosPorCategoria(idCategoria: Int): LiveData<List<PlatoModel>> {
        val platosLiveData = MutableLiveData<List<PlatoModel>>()
        hacerLlamada(idCategoria, platosLiveData, intentos = 0)
        return platosLiveData
    }

    private fun hacerLlamada(idCategoria: Int, liveData: MutableLiveData<List<PlatoModel>>, intentos: Int) {
        api.obtenerPlatosPorCategoria(idCategoria).enqueue(object : Callback<List<PlatoModel>> {
            override fun onResponse(call: Call<List<PlatoModel>>, response: Response<List<PlatoModel>>) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d("RepositoryPlatos", "Platos obtenidos: ${response.body()?.size}")
                    liveData.postValue(response.body()!!)
                } else {
                    Log.e("RepositoryPlatos", "Error respuesta: ${response.code()}")
                    liveData.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<List<PlatoModel>>, t: Throwable) {
                Log.e("RepositoryPlatos", "Error conexión (intento $intentos): ${t.message}")
                if (intentos < 2) {
                    // Reintentar hasta 2 veces si falla por "unexpected end of stream"
                    hacerLlamada(idCategoria, liveData, intentos + 1)
                } else {
                    liveData.postValue(emptyList())
                }
            }
        })
    }
}
