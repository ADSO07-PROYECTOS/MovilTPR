package com.sena.myapplication.views

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.sena.myapplication.R
import com.sena.myapplication.databinding.PlatoDetalleBinding
import com.sena.myapplication.models.ModelTamano
import com.sena.myapplication.services.ConexionServiceMenu
import com.sena.myapplication.viewmodels.ViewModelPlatoDetalle

/**
 * Pantalla de detalle de plato — Permite seleccionar tamaño, sabores y adiciones.
 *
 * Principio SRP: Solo gestiona la UI de configuración del plato.
 * La carga de extras se delega a [ViewModelPlatoDetalle].
 *
 * Principio OCP: Hereda la barra de navegación de [BaseActivity].
 * Si se añaden nuevos tipos de extras, se agregan nuevos observers
 * sin modificar los existentes.
 *
 * Principio DIP: Depende de la abstracción [ViewModelPlatoDetalle],
 * no de llamadas Retrofit directas.
 *
 * Recibe los datos básicos del plato vía Intent extras (ID_PLATO, NOMBRE_PLATO, etc.).
 * Al pulsar "Añadir", valida la selección y navega al carrito.
 */
class PlatoDetalleActivity : BaseActivity() {

    private lateinit var binding: PlatoDetalleBinding
    private lateinit var viewModel: ViewModelPlatoDetalle

    // Estado de selección del usuario
    private var listaTamanos: List<ModelTamano> = emptyList()
    private var tamanoSeleccionado: ModelTamano? = null
    private var precioTamanoSeleccionado: Double = 0.0
    // Estado de precios de sabores seleccionados
    private var precioSaboresTotalVal: Double = 0.0
    private var precioAdicionesTotal: Double = 0.0
    private var cantidad: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = PlatoDetalleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBarraNavegacion()

        // --- Recibir extras del Intent ---
        val idPlato     = intent.getIntExtra("ID_PLATO", -1)
        val nombrePlato = intent.getStringExtra("NOMBRE_PLATO") ?: ""
        val descripcion = intent.getStringExtra("DESCRIPCION_PLATO") ?: ""
        val imagenUrl   = intent.getStringExtra("IMAGEN_PLATO") ?: ""
        val precioBase  = intent.getDoubleExtra("PRECIO_PLATO", 0.0)

        // --- Configurar datos iniciales en la UI ---
        binding.tvNombrePlato.text      = nombrePlato
        binding.tvDescripcionPlato.text = descripcion
        precioTamanoSeleccionado = precioBase
        actualizarPrecioTotal()

        // Construir URL completa si solo viene el nombre del archivo
        val imagenCompleta = if (imagenUrl.startsWith("http")) imagenUrl
                             else "${ConexionServiceMenu.BASE_URL_IMAGENES}$imagenUrl"

        Glide.with(this)
            .load(imagenCompleta)
            .placeholder(R.drawable.plato1)
            .error(R.drawable.plato1)
            .into(binding.imgPlato)

        // --- Inicializar ViewModel y observar extras ---
        viewModel = ViewModelProvider(this)[ViewModelPlatoDetalle::class.java]
        viewModel.cargarExtras()

        if (idPlato != -1) {
            observarTamanos()
            observarSabores()
            observarAdiciones()
        } else {
            Toast.makeText(this, "Error: no se recibió el plato", Toast.LENGTH_SHORT).show()
        }

        // --- Listeners de botones ---
        binding.btnAtras.setOnClickListener { finish() }

        binding.btnMenos.setOnClickListener {
            if (cantidad > 1) {
                cantidad--
                binding.tvCantidad.text = cantidad.toString()
                actualizarPrecioTotal()
            }
        }

        binding.btnMas.setOnClickListener {
            cantidad++
            binding.tvCantidad.text = cantidad.toString()
            actualizarPrecioTotal()
        }

        binding.btnSeleccionarTamano.setOnClickListener {
            if (listaTamanos.isEmpty()) {
                viewModel.cargarExtras()
                Toast.makeText(this, "Cargando tamaños...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mostrarDialogoTamanos()
        }

        binding.btnAnadir.setOnClickListener {
            // Validar que se haya seleccionado un tamaño si hay tamaños disponibles
            if (listaTamanos.isNotEmpty() && tamanoSeleccionado == null) {
                Toast.makeText(this, "Selecciona un tamaño antes de añadir", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tamanoNombre = if (listaTamanos.isNotEmpty())
                binding.btnSeleccionarTamano.text.toString()
            else ""

            val intent = Intent(this, CarritoActivity::class.java).apply {
                putExtra("CARRITO_ID_PLATO",        idPlato)
                putExtra("CARRITO_NOMBRE",          nombrePlato)
                putExtra("CARRITO_TAMANO",          tamanoNombre)
                putExtra("CARRITO_CANTIDAD",        cantidad)
                putExtra("CARRITO_PRECIO_UNITARIO", precioTamanoSeleccionado + precioAdicionesTotal + precioSaboresTotalVal)
                putExtra("CARRITO_PRECIO_TOTAL",    (precioTamanoSeleccionado + precioAdicionesTotal + precioSaboresTotalVal) * cantidad)
            }
            startActivity(intent)
        }
    }

    // ==================== TAMAÑOS ====================

    /** Observa los tamaños del ViewModel y selecciona el primero por defecto. */
    private fun observarTamanos() {
        viewModel.tamanos.observe(this) { tamanos ->
            if (tamanos.isEmpty()) return@observe
            listaTamanos = tamanos
            seleccionarTamano(tamanos[0])
        }
    }

    /** Aplica el tamaño seleccionado a la UI y actualiza el precio. */
    private fun seleccionarTamano(tamano: ModelTamano) {
        tamanoSeleccionado = tamano
        precioTamanoSeleccionado = tamano.precio
        binding.btnSeleccionarTamano.text = tamano.nombre
        binding.tvPrecioTamano.text = getString(R.string.precio_total_label, tamano.precio.toInt())
        actualizarPrecioTotal()

        // Mostrar sabores solo si el tamaño permite más de 1
        binding.contenedorSabores.visibility = if (tamano.limiteSabores > 1)
            android.view.View.VISIBLE else android.view.View.GONE
    }

    /** Muestra un diálogo para elegir el tamaño del plato. */
    private fun mostrarDialogoTamanos() {
        val opciones = listaTamanos.map {
            "${it.nombre}  —  $${"%,.0f".format(it.precio)}"
        }.toTypedArray()
        var seleccionado = listaTamanos.indexOfFirst {
            it.id == tamanoSeleccionado?.id
        }.takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this, R.style.DialogTamano)
            .setTitle("Selecciona el tamaño")
            .setSingleChoiceItems(opciones, seleccionado) { _, which ->
                seleccionado = which
            }
            .setPositiveButton("Aceptar") { _, _ ->
                seleccionarTamano(listaTamanos[seleccionado])
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ==================== SABORES ====================

    /** Observa los sabores y genera las vistas de checkbox dinámicamente. */
    private fun observarSabores() {
        viewModel.sabores.observe(this) { sabores ->
            binding.layoutSabores.removeAllViews()
            precioSaboresTotalVal = 0.0
            if (sabores.isEmpty()) return@observe

            val inflater = LayoutInflater.from(this)
            sabores.forEach { sabor ->
                val item = inflater.inflate(R.layout.item_sabor, binding.layoutSabores, false)
                item.findViewById<TextView>(R.id.tvNombreSabor).text = sabor.nombre

                // Mostrar precio del sabor
                val tvPrecio = item.findViewById<TextView>(R.id.tvPrecioSabor)
                tvPrecio.text = getString(R.string.precio_total_label, sabor.precio.toInt())

                val checkbox = item.findViewById<android.widget.CheckBox>(R.id.checkboxSabor)
                checkbox.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        precioSaboresTotalVal += sabor.precio
                    } else {
                        precioSaboresTotalVal -= sabor.precio
                    }
                    actualizarPrecioTotal()
                }

                binding.layoutSabores.addView(item)
            }
        }
    }

    // ==================== ADICIONES ====================

    /** Observa las adiciones y genera las vistas con controles +/- dinámicamente. */
    private fun observarAdiciones() {
        viewModel.adiciones.observe(this) { adiciones ->
            binding.layoutAdiciones.removeAllViews()
            if (adiciones.isEmpty()) return@observe

            val inflater = LayoutInflater.from(this)
            adiciones.forEach { adicion ->
                val item = inflater.inflate(R.layout.item_adicion, binding.layoutAdiciones, false)

                item.findViewById<TextView>(R.id.tvNombreAdicion).text = adicion.nombre
                item.findViewById<MaterialButton>(R.id.tvPrecioAdicion).text =
                    getString(R.string.precio_total_label, adicion.precio.toInt())

                val tvCantidad = item.findViewById<TextView>(R.id.tvCantidadAdicion)
                var cantidadAdicion = 0

                item.findViewById<MaterialButton>(R.id.btnMenosAdicion).setOnClickListener {
                    if (cantidadAdicion > 0) {
                        cantidadAdicion--
                        precioAdicionesTotal -= adicion.precio
                        tvCantidad.text = cantidadAdicion.toString()
                        actualizarPrecioTotal()
                    }
                }
                item.findViewById<MaterialButton>(R.id.btnMasAdicion).setOnClickListener {
                    cantidadAdicion++
                    precioAdicionesTotal += adicion.precio
                    tvCantidad.text = cantidadAdicion.toString()
                    actualizarPrecioTotal()
                }

                binding.layoutAdiciones.addView(item)
            }
        }
    }

    // ==================== PRECIO ====================

    /** Recalcula y muestra el precio total: (tamaño + adiciones + sabores) × cantidad. */
    private fun actualizarPrecioTotal() {
        val total = (precioTamanoSeleccionado + precioAdicionesTotal + precioSaboresTotalVal) * cantidad
        binding.tvPrecioTotal.text = getString(R.string.precio_total_label, total.toInt())
    }
}

