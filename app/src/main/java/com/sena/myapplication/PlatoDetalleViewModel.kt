package com.sena.myapplication

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

class PlatoDetalleViewModel : ViewModel() {

    private val repository = RepositoryPlatoDetalle()

    val tamanos:   LiveData<List<Tamano>>  = repository.tamanos
    val sabores:   LiveData<List<Sabor>>   = repository.sabores
    val adiciones: LiveData<List<Adicion>> = repository.adiciones

    fun cargarExtras() = repository.cargarExtras()
}
