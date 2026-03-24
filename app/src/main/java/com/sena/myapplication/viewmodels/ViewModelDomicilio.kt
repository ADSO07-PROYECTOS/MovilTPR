package com.sena.myapplication.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sena.myapplication.models.DomicilioRequest
import com.sena.myapplication.models.DomicilioResponse
import com.sena.myapplication.services.ConexionServiceDomicilio
import kotlinx.coroutines.launch

class ViewModelDomicilio : ViewModel() {

    private val api = ConexionServiceDomicilio.instance

    // --- Resultado del pedido ---
    private val _pedidoExitoso = MutableLiveData<DomicilioResponse?>()
    val pedidoExitoso: LiveData<DomicilioResponse?> get() = _pedidoExitoso

    // --- Estados de UI ---
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    private val _enviando = MutableLiveData<Boolean>()
    val enviando: LiveData<Boolean> get() = _enviando

    // ------------------------------------------------------------------
    // Crear domicilio
    // ------------------------------------------------------------------

    fun crearDomicilio(body: DomicilioRequest) {
        _enviando.value = true
        _error.value = null
        Log.d(TAG, "Enviando domicilio: cliente=${body.cliente}, " +
              "domicilio=${body.domicilio}, productos=${body.productos}")
        enviarConReintento(body, intentos = 0)
    }

    private fun enviarConReintento(body: DomicilioRequest, intentos: Int) {
        viewModelScope.launch {
            try {
                val response = api.crearDomicilio(body)
                _enviando.value = false

                Log.d(TAG, "Response code: ${response.code()}")
                Log.d(TAG, "Response body: ${response.body()}")

                if (response.isSuccessful && response.body()?.status == "success") {
                    _pedidoExitoso.value = response.body()
                } else {
                    val errorBodyStr = try {
                        response.errorBody()?.string()
                    } catch (_: Exception) { null }
                    Log.e(TAG, "Error body: $errorBodyStr")

                    val mensajeError = response.body()?.message
                        ?: errorBodyStr
                        ?: "Error al crear el domicilio (código ${response.code()})"
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
        private const val TAG = "ViewModelDomicilio"
        private const val MAX_REINTENTOS = 2
    }
}

