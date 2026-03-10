package com.sena.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sena.myapplication.models.CategoriaViewModel

class MainActivity : BaseActivity() {

    private lateinit var rvCategorias: RecyclerView
    private lateinit var adapter: CategoriaAdapter
    private lateinit var viewModel: CategoriaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        configurarBarraNavegacion()

        rvCategorias = findViewById(R.id.rvCategorias)
        rvCategorias.layoutManager = GridLayoutManager(this, 2)

        viewModel = ViewModelProvider(this)[CategoriaViewModel::class.java]

        adapter = CategoriaAdapter(emptyList()) { abrirPlatos(it) }
        rvCategorias.adapter = adapter

        viewModel.obtenerCategorias().observe(this) { categorias ->
            adapter = CategoriaAdapter(categorias) { abrirPlatos(it) }
            rvCategorias.adapter = adapter
        }
    }

    private fun abrirPlatos(categoria: Categoria) {
        val intent = Intent(this, VerPlatosActivity::class.java).apply {
            putExtra("ID_CATEGORIA", categoria.id)
            putExtra("NOMBRE_CATEGORIA", categoria.nombre)
        }
        startActivity(intent)
    }
}
