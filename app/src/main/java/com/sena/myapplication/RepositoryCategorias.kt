/**
 * Proyecto  : Restaurante Tres Pasos - App Móvil
 * Archivo   : RepositoryCategorias.kt
 * Autor     : SENA ADSO - Centro Agropecuario de Buga
 * Fecha     : 2026
 * Descripción: Repositorio que gestiona las llamadas al endpoint
 *              /api/categorias. Implementa reintentos automáticos
 *              (máx. 2) para manejar la inestabilidad de Flask.
 */
package com.sena.myapplication

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RepositoryCategorias {

    // ------------------------------------------------------------------
    // Dependencias
    // ------------------------------------------------------------------
    private val api = RetrofitClient.instance

    // ------------------------------------------------------------------
    // Métodos públicos
    // ------------------------------------------------------------------

    /**
     * Solicita la lista de categorías al servidor.
     * @return LiveData con la lista de categorías observada desde el ViewModel.
     */
    fun obtenerCategorias(): LiveData<List<Categoria>> {
        val liveData = MutableLiveData<List<Categoria>>()
        hacerLlamada(liveData, intentos = 0)
        return liveData
    }

    // ------------------------------------------------------------------
    // Métodos privados
    // ------------------------------------------------------------------

    /** Realiza la llamada HTTP con lógica de reintentos. */
    private fun hacerLlamada(liveData: MutableLiveData<List<Categoria>>, intentos: Int) {
        api.obtenerCategorias().enqueue(object : Callback<List<Categoria>> {

            override fun onResponse(
                call: Call<List<Categoria>>,
                response: Response<List<Categoria>>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d("RepoCategorias", "Categorías cargadas: ${response.body()!!.size}")
                    liveData.postValue(response.body()!!)
                } else {
                    Log.e("RepoCategorias", "Error servidor: ${response.code()}")
                    liveData.postValue(emptyList())
                }
            }

            override fun onFailure(call: Call<List<Categoria>>, t: Throwable) {
                Log.e("RepoCategorias", "Fallo red (intento $intentos): ${t.message}")
                if (intentos < 2) {
                    hacerLlamada(liveData, intentos + 1)
                } else {
                    liveData.postValue(emptyList())
                }
            }
        })
    }
}
