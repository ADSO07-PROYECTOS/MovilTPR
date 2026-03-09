package com.sena.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VerPlatosActivity : BaseActivity() {

    private lateinit var recyclerViewPlatos: RecyclerView
    private lateinit var tvCategoriaNombre: TextView
    private lateinit var viewModel: PlatoViewModel
    private lateinit var adapter: PlatoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ver_platos)
        configurarBarraNavegacion()

        recyclerViewPlatos = findViewById(R.id.recyclerViewPlatos)
        tvCategoriaNombre  = findViewById(R.id.tvCategoriaNombre)
        recyclerViewPlatos.layoutManager = LinearLayoutManager(this)

        viewModel = ViewModelProvider(this)[PlatoViewModel::class.java]

        val idCategoria    = intent.getIntExtra("ID_CATEGORIA", -1)
        val nombreCategoria = intent.getStringExtra("NOMBRE_CATEGORIA")

        tvCategoriaNombre.text = nombreCategoria ?: ""

        adapter = PlatoAdapter(emptyList()) { abrirDetalle(it) }
        recyclerViewPlatos.adapter = adapter

        if (idCategoria != -1) {
            viewModel.obtenerPlatosPorCategoria(idCategoria).observe(this) { platos ->
                adapter.actualizarLista(platos)
            }
        } else {
            Toast.makeText(this, "Error: No se recibió la categoría", Toast.LENGTH_SHORT).show()
        }
    }

    private fun abrirDetalle(plato: Plato) {
        val intent = Intent(this, PlatoDetalleActivity::class.java).apply {
            putExtra("ID_PLATO",          plato.id)
            putExtra("NOMBRE_PLATO",      plato.nombre)
            putExtra("DESCRIPCION_PLATO", plato.descripcion)
            putExtra("IMAGEN_PLATO",      plato.imagen)
            putExtra("PRECIO_PLATO",      plato.precio)
        }
        startActivity(intent)
    }
}