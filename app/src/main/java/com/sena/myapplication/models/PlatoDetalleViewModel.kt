package com.sena.myapplication.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sena.myapplication.models.AdicionModel
import com.sena.myapplication.RepositoryPlatoDetalle

class PlatoDetalleViewModel : ViewModel() {

    private val repository = RepositoryPlatoDetalle()

    val tamanos:   LiveData<List<TamanoModel>>  = repository.tamanos
    val sabores:   LiveData<List<SaborModel>>   = repository.sabores
    val adiciones: LiveData<List<AdicionModel>> = repository.adiciones

    fun cargarExtras() = repository.cargarExtras()
}
