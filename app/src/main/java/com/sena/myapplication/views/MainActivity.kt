package com.sena.myapplication.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.sena.myapplication.adapters.AdapterCategoria
import com.sena.myapplication.databinding.ActivityMain3Binding
import com.sena.myapplication.models.ModelCategoria
import com.sena.myapplication.viewmodels.ViewModelCategoria

/**
 * Pantalla principal — Muestra la grilla de categorías del menú.
 *
 * Punto de entrada de la app (launcher). El usuario selecciona una
 * categoría para ver los platos disponibles.
 *
 * Principio SRP: La Activity solo observa el LiveData del ViewModel
 * y actualiza la UI. No contiene lógica de negocio ni llamadas de red.
 *
 * Principio DIP: Depende de la abstracción [ViewModelCategoria],
 * no de llamadas Retrofit directas.
 *
 * Principio OCP: Hereda la barra de navegación de [BaseActivity]
 * sin modificarla.
 */
class MainActivity : BaseActivity() {

    /** ViewBinding generado desde activity_main3.xml. */
    private lateinit var binding: ActivityMain3Binding

    private lateinit var adapter: AdapterCategoria
    private lateinit var viewModel: ViewModelCategoria

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding de forma segura
        binding = ActivityMain3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBarraNavegacion()

        // Configurar RecyclerView con layout de grilla 2 columnas
        binding.rvCategorias.layoutManager = GridLayoutManager(this, 2)
        adapter = AdapterCategoria(emptyList()) { abrirPlatos(it) }
        binding.rvCategorias.adapter = adapter

        // Inicializar ViewModel y observar datos reactivamente
        viewModel = ViewModelProvider(this)[ViewModelCategoria::class.java]

        viewModel.categorias.observe(this) { categorias ->
            adapter.actualizarLista(categorias)
        }

        viewModel.error.observe(this) { mensajeError ->
            mensajeError?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Disparar la carga inicial de categorías
        viewModel.obtenerCategorias()
    }

    /**
     * Navega a la pantalla de platos de la categoría seleccionada.
     * Pasa ID y nombre como extras del Intent.
     */
    private fun abrirPlatos(categoria: ModelCategoria) {
        val intent = Intent(this, VerPlatosActivity::class.java).apply {
            putExtra("ID_CATEGORIA", categoria.id)
            putExtra("NOMBRE_CATEGORIA", categoria.nombre)
        }
        startActivity(intent)
    }
}

