package com.sena.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class PlatoViewModel : ViewModel() {

    private val repository = RepositoryPlatos()
    private var platosLiveData: LiveData<List<Plato>>? = null
    private var ultimaCategoriaId: Int = -1

    fun obtenerPlatosPorCategoria(idCategoria: Int): LiveData<List<Plato>> {
        if (platosLiveData == null || ultimaCategoriaId != idCategoria) {
            ultimaCategoriaId = idCategoria
            platosLiveData = repository.obtenerPlatosPorCategoria(idCategoria)
        }
        return platosLiveData!!
    }
}
