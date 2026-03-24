package com.sena.myapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.myapplication.models.ModelTematica
import com.sena.myapplication.models.ReservaRequest
import com.sena.myapplication.models.ReservaResponse
import com.sena.myapplication.services.ConexionServiceReserva
import kotlinx.coroutines.launch


class ViewModelReserva : ViewModel() {

    private val api = ConexionServiceReserva.instance

    // --- Temáticas ---
    private val _tematicas = MutableLiveData<List<ModelTematica>>()
    val tematicas: LiveData<List<ModelTematica>> get() = _tematicas

    // --- Resultado de crear reserva ---
    private val _reservaExitosa = MutableLiveData<ReservaResponse?>()
    val reservaExitosa: LiveData<ReservaResponse?> get() = _reservaExitosa

    // --- Estados de UI ---
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _enviando = MutableLiveData<Boolean>()
    val enviando: LiveData<Boolean> get() = _enviando

    // ------------------------------------------------------------------
    // Cargar temáticas
    // ------------------------------------------------------------------

    fun cargarTematicas() {
        cargarTematicasConReintento(intentos = 0)
    }

    private fun cargarTematicasConReintento(intentos: Int) {
        viewModelScope.launch {
            try {
                val response = api.obtenerTematicas()
                if (response.isSuccessful && response.body() != null) {
                    _tematicas.value = response.body()!!
                    Log.d(TAG, "Temáticas cargadas: ${response.body()!!.size}")
                } else if (intentos < MAX_REINTENTOS) {
                    cargarTematicasConReintento(intentos + 1)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando temáticas (intento $intentos): ${e.message}")
                if (intentos < MAX_REINTENTOS) {
                    cargarTematicasConReintento(intentos + 1)
                } else {
                    _error.value = "Error al cargar temáticas"
                }
            }
        }
    }

    fun crearReserva(body: ReservaRequest) {
        _enviando.value = true
        _error.value = null
        Log.d(TAG, "Enviando reserva: cliente=${body.cliente}, " +
              "reserva=${body.reserva}, pedido=${body.pedido}")
        enviarConReintento(body, intentos = 0)
    }

    private fun enviarConReintento(body: ReservaRequest, intentos: Int) {
        viewModelScope.launch {
            try {
                val response = api.crearReserva(body)
                _enviando.value = false

                Log.d(TAG, "Response code: ${response.code()}")
                Log.d(TAG, "Response body: ${response.body()}")

                if (response.isSuccessful && response.body()?.status == "success") {
                    _reservaExitosa.value = response.body()
                } else {
                    val errorBodyStr = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) { null }
                    Log.e(TAG, "Error body: $errorBodyStr")

                    val mensajeError = response.body()?.message
                        ?: errorBodyStr
                        ?: "Error al crear la reserva (código ${response.code()})"
                    _error.value = mensajeError
                }
            } catch (e: Exception) {
                Log.e(TAG, "onFailure: ${e.message}", e)
                if (intentos < MAX_REINTENTOS) {
                    enviarConReintento(body, intentos + 1)
                    return@launch
                }
                _enviando.value = false
                _error.value = "Error de red: ${e.message}"
            }
        }
    }

    companion object {
        private const val TAG = "ViewModelReserva"
        private const val MAX_REINTENTOS = 2
    }
}

