package com.sena.myapplication.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sena.myapplication.models.PlatoModel
import com.sena.myapplication.RepositoryPlatos

class PlatoViewModel : ViewModel() {

    private val repository = RepositoryPlatos()
    private var platosLiveData: LiveData<List<PlatoModel>>? = null
    private var ultimaCategoriaId: Int = -1

    fun obtenerPlatosPorCategoria(idCategoria: Int): LiveData<List<PlatoModel>> {
        if (platosLiveData == null || ultimaCategoriaId != idCategoria) {
            ultimaCategoriaId = idCategoria
            platosLiveData = repository.obtenerPlatosPorCategoria(idCategoria)
        }
        return platosLiveData!!
    }
}
