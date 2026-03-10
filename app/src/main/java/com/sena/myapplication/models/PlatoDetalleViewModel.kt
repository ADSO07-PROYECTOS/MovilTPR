package com.sena.myapplication.models

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.sena.myapplication.Adicion
import com.sena.myapplication.RepositoryPlatoDetalle
import com.sena.myapplication.Sabor
import com.sena.myapplication.models.TamanoModel

class PlatoDetalleViewModel : ViewModel() {

    private val repository = RepositoryPlatoDetalle()

    val tamanos:   LiveData<List<TamanoModel>>  = repository.tamanos
    val sabores:   LiveData<List<Sabor>>   = repository.sabores
    val adiciones: LiveData<List<Adicion>> = repository.adiciones

    fun cargarExtras() = repository.cargarExtras()
}
