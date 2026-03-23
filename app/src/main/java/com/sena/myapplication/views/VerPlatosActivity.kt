package com.sena.myapplication.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.sena.myapplication.adapters.AdapterPlato
import com.sena.myapplication.databinding.ActivityVerPlatosBinding
import com.sena.myapplication.models.ModelPlato
import com.sena.myapplication.viewmodels.ViewModelPlato

/**
 * Pantalla de platos por categoría — Lista los platos disponibles.
 *
 * El usuario selecciona un plato para ver sus detalles (tamaño, sabor, adiciones).
 * Recibe ID_CATEGORIA y NOMBRE_CATEGORIA como extras del Intent.
 */
class VerPlatosActivity : BaseActivity() {

    private lateinit var binding: ActivityVerPlatosBinding
    private lateinit var viewModel: ViewModelPlato
    private lateinit var adapter: AdapterPlato

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityVerPlatosBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBarraNavegacion()

        // Configurar RecyclerView con layout lineal
        binding.recyclerViewPlatos.layoutManager = LinearLayoutManager(this)
        adapter = AdapterPlato(emptyList()) { abrirDetalle(it) }
        binding.recyclerViewPlatos.adapter = adapter

        // Recibir extras del Intent
        val idCategoria     = intent.getIntExtra("ID_CATEGORIA", -1)
        val nombreCategoria = intent.getStringExtra("NOMBRE_CATEGORIA")
        binding.tvCategoriaNombre.text = nombreCategoria ?: ""

        // Inicializar ViewModel y observar datos
        viewModel = ViewModelProvider(this)[ViewModelPlato::class.java]

        viewModel.platos.observe(this) { platos ->
            adapter.actualizarLista(platos)
        }

        viewModel.error.observe(this) { mensajeError ->
            mensajeError?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }

        // Validar que se recibió la categoría y disparar carga
        if (idCategoria != -1) {
            viewModel.obtenerPlatosPorCategoria(idCategoria)
        } else {
            Toast.makeText(this, "Error: No se recibió la categoría", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Navega al detalle del plato seleccionado.
     * Pasa los datos básicos del plato como extras del Intent.
     */
    private fun abrirDetalle(plato: ModelPlato) {
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

