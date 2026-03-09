package com.sena.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class CategoriaViewModel : ViewModel() {
    private val repository = RepositoryCategorias()

    private var categoriasLiveData: LiveData<List<Categoria>>? = null

    fun obtenerCategorias(): LiveData<List<Categoria>> {
        if (categoriasLiveData == null) {
            categoriasLiveData = repository.obtenerCategorias()
        }
        return categoriasLiveData!!
    }

    fun recargarCategorias(): LiveData<List<Categoria>> {
        categoriasLiveData = repository.obtenerCategorias()
        return categoriasLiveData!!
    }
}
