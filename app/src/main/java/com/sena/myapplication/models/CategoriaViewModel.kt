package com.sena.myapplication.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sena.myapplication.Categoria
import com.sena.myapplication.RepositoryCategorias

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
