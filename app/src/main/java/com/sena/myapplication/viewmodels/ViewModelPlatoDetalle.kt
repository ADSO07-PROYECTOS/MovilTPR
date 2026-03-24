package com.sena.myapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.myapplication.models.ModelAdicion
import com.sena.myapplication.models.ModelSabor
import com.sena.myapplication.models.ModelTamano
import com.sena.myapplication.services.ConexionServiceMenu
import kotlinx.coroutines.launch


class ViewModelPlatoDetalle : ViewModel() {

    private val api = ConexionServiceMenu.instance

    // LiveData encapsulado: mutable privado, inmutable público
    private val _tamanos = MutableLiveData<List<ModelTamano>>()
    val tamanos: LiveData<List<ModelTamano>> get() = _tamanos

    private val _sabores = MutableLiveData<List<ModelSabor>>()
    val sabores: LiveData<List<ModelSabor>> get() = _sabores

    private val _adiciones = MutableLiveData<List<ModelAdicion>>()
    val adiciones: LiveData<List<ModelAdicion>> get() = _adiciones

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error


    fun cargarExtras() {
        realizarLlamada(intentos = 0)
    }

    private fun realizarLlamada(intentos: Int) {
        viewModelScope.launch {
            try {
                val response = api.obtenerExtras()

                if (response.isSuccessful && response.body() != null) {
                    val body = response.body()!!
                    _tamanos.value = body.tamanos
                    _sabores.value = body.sabores
                    _adiciones.value = body.adiciones
                    _error.value = null
                    Log.d(TAG,
                        "Extras cargados → tamaños:${body.tamanos.size} " +
                        "sabores:${body.sabores.size} adiciones:${body.adiciones.size}")
                } else {
                    Log.e(TAG, "Error /api/extras: ${response.code()}")
                    publicarVacios()
                    _error.value = "Error del servidor (código ${response.code()})"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fallo conexión extras (intento $intentos): ${e.message}")
                if (intentos < MAX_REINTENTOS) {
                    realizarLlamada(intentos + 1)
                    return@launch
                }
                publicarVacios()
                _error.value = "Error de conexión: ${e.message}"
            }
        }
    }


    private fun publicarVacios() {
        _tamanos.value = emptyList()
        _sabores.value = emptyList()
        _adiciones.value = emptyList()
    }

    companion object {
        private const val TAG = "VMPlatoDetalle"
        private const val MAX_REINTENTOS = 2
    }
}

