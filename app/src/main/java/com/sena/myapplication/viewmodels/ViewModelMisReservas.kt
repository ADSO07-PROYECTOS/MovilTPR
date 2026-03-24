package com.sena.myapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.myapplication.models.ActualizarReservaRequest
import com.sena.myapplication.models.ModelMiReserva
import com.sena.myapplication.models.ModelTematica
import com.sena.myapplication.models.ReservaResponse
import com.sena.myapplication.services.ConexionServiceMisReservas
import kotlinx.coroutines.launch


class ViewModelMisReservas : ViewModel() {

    private val api = ConexionServiceMisReservas.instance

    // --- Temáticas (para el selector de edición) ---
    private val _tematicas = MutableLiveData<List<ModelTematica>>()
    val tematicas: LiveData<List<ModelTematica>> get() = _tematicas

    // --- Resultado de búsqueda ---
    private val _reservas = MutableLiveData<List<ModelMiReserva>>()
    val reservas: LiveData<List<ModelMiReserva>> get() = _reservas

    // --- Resultado de actualización ---
    private val _actualizacionExitosa = MutableLiveData<ReservaResponse?>()
    val actualizacionExitosa: LiveData<ReservaResponse?> get() = _actualizacionExitosa

    // --- Resultado de eliminación ---
    private val _eliminacionExitosa = MutableLiveData<ReservaResponse?>()
    val eliminacionExitosa: LiveData<ReservaResponse?> get() = _eliminacionExitosa

    // --- Estados de UI ---
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando


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
                } else {
                    Log.e(TAG, "Error cargando temáticas: HTTP ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error cargando temáticas (intento $intentos): ${e.message}")
                if (intentos < MAX_REINTENTOS) {
                    cargarTematicasConReintento(intentos + 1)
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Buscar reservas por cédula
    // ------------------------------------------------------------------

    fun buscarPorCedula(cedula: String) {
        _cargando.value = true
        _error.value = null
        buscarConReintento(cedula, intentos = 0)
    }

    private fun buscarConReintento(cedula: String, intentos: Int) {
        viewModelScope.launch {
            try {
                val response = api.buscarReservasPorCedula(cedula)
                _cargando.value = false

                if (response.isSuccessful && response.body() != null) {
                    val wrapper = response.body()!!
                    val lista = wrapper.reservas
                    _reservas.value = lista
                    if (lista.isEmpty()) {
                        _error.value = "No se encontraron reservas para esta cédula"
                    }
                    Log.d(TAG, "Reservas encontradas: ${lista.size}")
                } else {
                    // Leer cuerpo de error para diagnóstico
                    val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    Log.e(TAG, "Error HTTP ${response.code()}: $errorBody")

                    _reservas.value = emptyList()
                    _error.value = when (response.code()) {
                        400 -> "Cédula requerida"
                        404 -> "Ruta no encontrada en el servidor (404)"
                        500 -> "Error interno del servidor (500): ${errorBody ?: "sin detalles"}"
                        else -> "Error del servidor (código ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error buscando reservas (intento $intentos): ${e.message}", e)
                if (intentos < MAX_REINTENTOS) {
                    buscarConReintento(cedula, intentos + 1)
                    return@launch
                }
                _cargando.value = false
                _reservas.value = emptyList()
                _error.value = "Error de conexión: ${e.message}"
            }
        }
    }

    // ------------------------------------------------------------------
    // Actualizar reserva
    // ------------------------------------------------------------------

    fun actualizarReserva(reservaId: Int, body: ActualizarReservaRequest) {
        _cargando.value = true
        _error.value = null
        actualizarConReintento(reservaId, body, intentos = 0)
    }

    private fun actualizarConReintento(reservaId: Int, body: ActualizarReservaRequest, intentos: Int) {
        viewModelScope.launch {
            try {
                val response = api.actualizarReserva(reservaId, body)
                _cargando.value = false

                if (response.isSuccessful) {
                    _actualizacionExitosa.value = response.body()
                    Log.d(TAG, "Reserva $reservaId actualizada")
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    Log.e(TAG, "Error actualizando HTTP ${response.code()}: $errorBody")
                    _error.value = when (response.code()) {
                        404 -> "Ruta PUT no encontrada en el servidor (404)"
                        500 -> "Error interno del servidor al actualizar"
                        else -> "Error al actualizar (código ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando (intento $intentos): ${e.message}", e)
                if (intentos < MAX_REINTENTOS) {
                    actualizarConReintento(reservaId, body, intentos + 1)
                    return@launch
                }
                _cargando.value = false
                _error.value = "Error de red: ${e.message}"
            }
        }
    }

    // ------------------------------------------------------------------
    // Eliminar reserva
    // ------------------------------------------------------------------

    fun eliminarReserva(reservaId: Int) {
        _cargando.value = true
        _error.value = null
        eliminarConReintento(reservaId, intentos = 0)
    }

    private fun eliminarConReintento(reservaId: Int, intentos: Int) {
        viewModelScope.launch {
            try {
                val response = api.eliminarReserva(reservaId)
                _cargando.value = false

                if (response.isSuccessful) {
                    _eliminacionExitosa.value = response.body()
                    Log.d(TAG, "Reserva $reservaId eliminada")
                } else {
                    val errorBody = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    Log.e(TAG, "Error eliminando HTTP ${response.code()}: $errorBody")
                    _error.value = when (response.code()) {
                        404 -> "Ruta DELETE no encontrada en el servidor (404)"
                        500 -> "Error interno del servidor al eliminar"
                        else -> "Error al eliminar (código ${response.code()})"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando (intento $intentos): ${e.message}", e)
                if (intentos < MAX_REINTENTOS) {
                    eliminarConReintento(reservaId, intentos + 1)
                    return@launch
                }
                _cargando.value = false
                _error.value = "Error de red: ${e.message}"
            }
        }
    }

    companion object {
        private const val TAG = "VMMisReservas"
        private const val MAX_REINTENTOS = 2
    }
}
