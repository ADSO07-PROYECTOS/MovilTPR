package com.sena.myapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.myapplication.models.ModelCategoria
import com.sena.myapplication.services.ConexionServiceMenu
import kotlinx.coroutines.launch

/**
 * ViewModel para la pantalla de categorías (MainActivity).
 *
 * Principio SRP: Solo gestiona la obtención y exposición reactiva
 * de la lista de categorías.
 *
 * Principio DIP (Dependency Inversion): Depende de la interfaz
 * [ConexionServiceMenu] y no de una implementación concreta de Retrofit.
 *
 * Usa coroutines con [viewModelScope] para llamadas asíncronas.
 * Implementa reintento automático (máx. 2) ante fallos de red.
 */
class ViewModelCategoria : ViewModel() {

    // ------------------------------------------------------------------
    // Dependencias — Instancia del servicio de menú
    // ------------------------------------------------------------------
    private val api = ConexionServiceMenu.instance

    // ------------------------------------------------------------------
    // LiveData — _mutableLiveData privado, LiveData público (encapsulación)
    // ------------------------------------------------------------------
    private val _categorias = MutableLiveData<List<ModelCategoria>>()
    val categorias: LiveData<List<ModelCategoria>> get() = _categorias

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _cargando = MutableLiveData<Boolean>()
    val cargando: LiveData<Boolean> get() = _cargando

    // ------------------------------------------------------------------
    // Métodos públicos
    // ------------------------------------------------------------------

    /** Solicita las categorías al servidor con reintentos automáticos. */
    fun obtenerCategorias() {
        _cargando.value = true
        _error.value = null
        realizarLlamada(intentos = 0)
    }

    // ------------------------------------------------------------------
    // Lógica privada — Coroutine con reintento
    // ------------------------------------------------------------------

    /**
     * Ejecuta la llamada suspendida dentro de [viewModelScope].
     * Si falla por red, reintenta hasta [MAX_REINTENTOS] veces.
     */
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

