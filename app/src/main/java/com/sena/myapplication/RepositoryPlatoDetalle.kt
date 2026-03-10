package com.sena.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import android.util.Log
import com.sena.myapplication.conexion.RetrofitClient
import com.sena.myapplication.models.TamanoModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepositoryPlatoDetalle {

    private val api = RetrofitClient.instance

    private val _tamanos   = MutableLiveData<List<TamanoModel>>()
    private val _sabores   = MutableLiveData<List<Sabor>>()
    private val _adiciones = MutableLiveData<List<Adicion>>()

    val tamanos:   LiveData<List<TamanoModel>>   = _tamanos
    val sabores:   LiveData<List<Sabor>>    = _sabores
    val adiciones: LiveData<List<Adicion>>  = _adiciones

    /** Una sola llamada carga tamaños, sabores y adiciones desde /api/extras */
    fun cargarExtras() {
        hacerLlamada(intentos = 0)
    }

    private fun hacerLlamada(intentos: Int) {
        api.obtenerExtras().enqueue(object : Callback<Extras> {
            override fun onResponse(call: Call<Extras>, response: Response<Extras>) {
                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _tamanos.postValue(body.tamanoModels)
                    _sabores.postValue(body.sabores)
                    _adiciones.postValue(body.adiciones)
                    Log.d("RepoPlatoDetalle",
                        "Extras cargados → tamaños:${body.tamanoModels.size} " +
                        "sabores:${body.sabores.size} adiciones:${body.adiciones.size}")
                } else {
                    Log.e("RepoPlatoDetalle", "Error /api/extras: ${response.code()}")
                    _tamanos.postValue(emptyList())
                    _sabores.postValue(emptyList())
                    _adiciones.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<Extras>, t: Throwable) {
                Log.e("RepoPlatoDetalle", "Fallo conexión extras (intento $intentos): ${t.message}")
                if (intentos < 2) {
                    hacerLlamada(intentos + 1)
                } else {
                    _tamanos.postValue(emptyList())
                    _sabores.postValue(emptyList())
                    _adiciones.postValue(emptyList())
                }
            }
        })
    }
}
