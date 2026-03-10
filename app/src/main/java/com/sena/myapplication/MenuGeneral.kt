package com.sena.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sena.myapplication.adapter.CategoriaAdapter
import com.sena.myapplication.models.CategoriaModel
import com.sena.myapplication.models.CategoriaViewModel

class MenuGeneral : BaseActivity() {

    private lateinit var recyclerViewCategorias: RecyclerView
    private lateinit var adapter: CategoriaAdapter
    private lateinit var viewModel: CategoriaViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_general)
        configurarBarraNavegacion()
        // Enlazamos la lista
        recyclerViewCategorias = findViewById(R.id.recyclerViewPizzas)
        recyclerViewCategorias.layoutManager = GridLayoutManager(this, 2)

        // Inicializar el ViewModel
        viewModel = ViewModelProvider(this).get(CategoriaViewModel::class.java)

        adapter = CategoriaAdapter(emptyList()) { categoriaSeleccionada ->
          abrirPlatosDelaCategoría(categoriaSeleccionada)
        }
        recyclerViewCategorias.adapter = adapter

        // Observar los cambios en las categorías de forma reactiva
        viewModel.obtenerCategorias().observe(this) { categorias ->
            adapter = CategoriaAdapter(categorias) { categoriaSeleccionada ->
              abrirPlatosDelaCategoría(categoriaSeleccionada)
            }
            recyclerViewCategorias.adapter = adapter
        }
    }

    override fun onResume() {
        super.onResume()
        // Recargar categorías automáticamente cuando vuelves a la pantalla
        recargarCategorias()
    }

    private fun recargarCategorias() {
        viewModel.recargarCategorias().observe(this) { categorias ->
            adapter = CategoriaAdapter(categorias) { categoriaSeleccionada ->
              abrirPlatosDelaCategoría(categoriaSeleccionada)
            }
            recyclerViewCategorias.adapter = adapter
        }
    }

    private fun abrirPlatosDelaCategoría(categoriaModelSeleccionada: CategoriaModel) {
        val intent = Intent(this@MenuGeneral, VerPlatosActivity::class.java)
        intent.putExtra("ID_CATEGORIA", categoriaModelSeleccionada.id)
        intent.putExtra("NOMBRE_CATEGORIA", categoriaModelSeleccionada.nombre)
        startActivity(intent)
    }
}
