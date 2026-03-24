package com.sena.myapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.myapplication.models.ModelPlato
import com.sena.myapplication.services.ConexionServiceMenu
import kotlinx.coroutines.launch


class ViewModelPlato : ViewModel() {

    private val api = ConexionServiceMenu.instance

    private val _platos = MutableLiveData<List<ModelPlato>>()
    val platos: LiveData<List<ModelPlato>> get() = _platos

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    private var ultimaCategoriaId: Int = -1

    fun obtenerPlatosPorCategoria(idCategoria: Int) {
        if (idCategoria == ultimaCategoriaId && _platos.value != null) return
        ultimaCategoriaId = idCategoria
        _cargando.value = true
        _error.value = null
        realizarLlamada(idCategoria, intentos = 0)
    }

    private fun realizarLlamada(idCategoria: Int, intentos: Int) {
        viewModelScope.launch {
            try {
                val response = api.obtenerPlatosPorCategoria(idCategoria)

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "Platos obtenidos: ${response.body()!!.size}")
                    _platos.value = response.body()!!
                    _error.value = null
                } else {
                    Log.e(TAG, "Error respuesta: ${response.code()}")
                    _platos.value = emptyList()
                    _error.value = "Error del servidor (código ${response.code()})"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error conexión (intento $intentos): ${e.message}")
                if (intentos < MAX_REINTENTOS) {
                    realizarLlamada(idCategoria, intentos + 1)
                    return@launch
                }
                _platos.value = emptyList()
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    companion object {
        private const val TAG = "ViewModelPlato"
        private const val MAX_REINTENTOS = 2
    }
}

