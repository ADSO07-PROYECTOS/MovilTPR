package com.sena.myapplication.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sena.myapplication.models.CategoriaModel
import com.sena.myapplication.RepositoryCategorias

class CategoriaViewModel : ViewModel() {
    private val repository = RepositoryCategorias()

    private var categoriasLiveData: LiveData<List<CategoriaModel>>? = null

    fun obtenerCategorias(): LiveData<List<CategoriaModel>> {
        if (categoriasLiveData == null) {
            categoriasLiveData = repository.obtenerCategorias()
        }
        return categoriasLiveData!!
    }

    fun recargarCategorias(): LiveData<List<CategoriaModel>> {
        categoriasLiveData = repository.obtenerCategorias()
        return categoriasLiveData!!
    }
}
