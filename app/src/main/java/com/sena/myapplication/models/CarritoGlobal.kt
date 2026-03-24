package com.sena.myapplication.models


object CarritoGlobal {

    private var item: ModelCarritoItem? = null

    fun guardar(item: ModelCarritoItem) {
        this.item = item
    }

    fun obtener(): ModelCarritoItem? = item

    fun tieneProducto(): Boolean = item != null

    fun limpiar() {
        item = null
    }
}

