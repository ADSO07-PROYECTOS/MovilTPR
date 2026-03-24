package com.sena.myapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.myapplication.models.ModelCategoria
import com.sena.myapplication.services.ConexionServiceMenu
import kotlinx.coroutines.launch


class ViewModelCategoria : ViewModel() {
    private val api = ConexionServiceMenu.instance


    private val _categorias = MutableLiveData<List<ModelCategoria>>()
    val categorias: LiveData<List<ModelCategoria>> get() = _categorias

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    //
    fun obtenerCategorias() {
        _cargando.value = true
        _error.value = null
        realizarLlamada(intentos = 0)
    }

    private fun realizarLlamada(intentos: Int) {
        viewModelScope.launch {
            try {
                val response = api.obtenerCategorias()

                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "Categorías cargadas: ${response.body()!!.size}")
                    _categorias.value = response.body()!!
                    _error.value = null
                } else {
                    Log.e(TAG, "Error servidor: ${response.code()}")
                    _categorias.value = emptyList()
                    _error.value = "Error del servidor (código ${response.code()})"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Fallo red (intento $intentos): ${e.message}")
                if (intentos < MAX_REINTENTOS) {
                    realizarLlamada(intentos + 1)
                    return@launch
                }
                _categorias.value = emptyList()
                _error.value = "Error de conexión: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    companion object {
        private const val TAG = "ViewModelCategoria"
        private const val MAX_REINTENTOS = 2
    }
}

