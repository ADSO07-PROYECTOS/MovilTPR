package com.sena.myapplication.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.sena.myapplication.adapters.AdapterCategoria
import com.sena.myapplication.databinding.ActivityMain3Binding
import com.sena.myapplication.models.CarritoGlobal
import com.sena.myapplication.models.ModelCategoria
import com.sena.myapplication.viewmodels.ViewModelCategoria

class MainActivity : BaseActivity() {

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

        // --- Botones de Reservar y Domicilio ---
        binding.btnReservar.setOnClickListener {
            val intent = Intent(this, DatosClienteActivity::class.java).apply {
                putExtra("FLUJO_DOMICILIO", false)
            }
            startActivity(intent)
        }

        binding.btnDomicilio.setOnClickListener {
            if (!CarritoGlobal.tieneProducto()) {
                Toast.makeText(this, "Primero debes agregar un producto al carrito", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val item = CarritoGlobal.obtener()!!
            val intent = Intent(this, DatosClienteActivity::class.java).apply {
                putExtra("CARRITO_ID_PLATO",        item.idPlato)
                putExtra("CARRITO_NOMBRE",          item.nombrePlato)
                putExtra("CARRITO_TAMANO",          item.nombreTamano)
                putExtra("CARRITO_CANTIDAD",        item.cantidad)
                putExtra("CARRITO_PRECIO_UNITARIO", item.precioUnitario)
                putExtra("FLUJO_DOMICILIO",         true)
            }
            startActivity(intent)
        }
    }


    private fun abrirPlatos(categoria: ModelCategoria) {
        val intent = Intent(this, VerPlatosActivity::class.java).apply {
            putExtra("ID_CATEGORIA", categoria.id)
            putExtra("NOMBRE_CATEGORIA", categoria.nombre)
        }
        startActivity(intent)
    }
}

