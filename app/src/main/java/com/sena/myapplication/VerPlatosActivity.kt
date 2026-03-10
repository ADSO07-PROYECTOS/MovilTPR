package com.sena.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sena.myapplication.adapter.PlatoAdapter
import com.sena.myapplication.models.PlatoModel
import com.sena.myapplication.models.PlatoViewModel

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

    private fun abrirDetalle(platoModel: PlatoModel) {
        val intent = Intent(this, PlatoDetalleActivity::class.java).apply {
            putExtra("ID_PLATO",          platoModel.id)
            putExtra("NOMBRE_PLATO",      platoModel.nombre)
            putExtra("DESCRIPCION_PLATO", platoModel.descripcion)
            putExtra("IMAGEN_PLATO",      platoModel.imagen)
            putExtra("PRECIO_PLATO",      platoModel.precio)
        }
        startActivity(intent)
    }
}
