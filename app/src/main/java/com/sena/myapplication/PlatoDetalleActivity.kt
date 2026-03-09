package com.sena.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton

class PlatoDetalleActivity : BaseActivity() {

    private lateinit var tvNombrePlato: TextView
    private lateinit var imgPlato: ImageView
    private lateinit var tvDescripcionPlato: TextView
    private lateinit var btnSeleccionarTamano: MaterialButton
    private lateinit var layoutSabores: LinearLayout
    private lateinit var layoutAdiciones: LinearLayout
    private lateinit var btnAtras: ImageButton
    private lateinit var btnMenos: ImageButton
    private lateinit var btnMas: ImageButton
    private lateinit var tvCantidad: TextView
    private lateinit var tvPrecioTotal: TextView
    private lateinit var btnAnadir: Button

    private lateinit var viewModel: PlatoDetalleViewModel

    private var listaTamanos: List<Tamano> = emptyList()
    private var precioTamanoSeleccionado: Double = 0.0
    private var precioAdicionesTotal: Double = 0.0
    private var cantidad: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.plato_detalle)
        configurarBarraNavegacion()

        // Vincular vistas
        tvNombrePlato         = findViewById(R.id.tvNombrePlato)
        imgPlato              = findViewById(R.id.imgPlato)
        tvDescripcionPlato    = findViewById(R.id.tvDescripcionPlato)
        btnSeleccionarTamano  = findViewById(R.id.btnSeleccionarTamano)
        layoutSabores         = findViewById(R.id.layoutSabores)
        layoutAdiciones    = findViewById(R.id.layoutAdiciones)
        btnAtras           = findViewById(R.id.btnAtras)
        btnMenos           = findViewById(R.id.btnMenos)
        btnMas             = findViewById(R.id.btnMas)
        tvCantidad         = findViewById(R.id.tvCantidad)
        tvPrecioTotal      = findViewById(R.id.tvPrecioTotal)
        btnAnadir          = findViewById(R.id.btnAnadir)

        val idPlato     = intent.getIntExtra("ID_PLATO", -1)
        val nombrePlato = intent.getStringExtra("NOMBRE_PLATO") ?: ""
        val descripcion = intent.getStringExtra("DESCRIPCION_PLATO") ?: ""
        val imagenUrl   = intent.getStringExtra("IMAGEN_PLATO") ?: ""
        val precioBase  = intent.getDoubleExtra("PRECIO_PLATO", 0.0)

        tvNombrePlato.text       = nombrePlato
        tvDescripcionPlato.text  = descripcion
        precioTamanoSeleccionado = precioBase
        actualizarPrecioTotal()

        Glide.with(this)
            .load(imagenUrl)
            .placeholder(R.drawable.plato1)
            .error(R.drawable.plato1)
            .into(imgPlato)

        viewModel = ViewModelProvider(this)[PlatoDetalleViewModel::class.java]
        viewModel.cargarExtras()

        if (idPlato != -1) {
            cargarTamanos()
            cargarSabores()
            cargarAdiciones()
        } else {
            Toast.makeText(this, "Error: no se recibió el plato", Toast.LENGTH_SHORT).show()
        }

        btnAtras.setOnClickListener { finish() }

        btnMenos.setOnClickListener {
            if (cantidad > 1) { cantidad--; tvCantidad.text = cantidad.toString(); actualizarPrecioTotal() }
        }
        btnMas.setOnClickListener {
            cantidad++; tvCantidad.text = cantidad.toString(); actualizarPrecioTotal()
        }

        btnAnadir.setOnClickListener {
            val tamanoNombre = if (listaTamanos.isNotEmpty())
                btnSeleccionarTamano.text.toString()
            else ""

            val intent = Intent(this, CarritoActivity::class.java).apply {
                putExtra("CARRITO_NOMBRE",          nombrePlato)
                putExtra("CARRITO_TAMANO",          tamanoNombre)
                putExtra("CARRITO_CANTIDAD",        cantidad)
                putExtra("CARRITO_PRECIO_UNITARIO", precioTamanoSeleccionado + precioAdicionesTotal)
                putExtra("CARRITO_PRECIO_TOTAL",    (precioTamanoSeleccionado + precioAdicionesTotal) * cantidad)
            }
            startActivity(intent)
        }

        // Listener del botón de tamaño siempre activo
        btnSeleccionarTamano.setOnClickListener {
            if (listaTamanos.isEmpty()) {
                // Reintentar cargar extras si aún no llegaron
                viewModel.cargarExtras()
                Toast.makeText(this, "Cargando tamaños...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            mostrarDialogoTamanos()
        }
    }

    // ---------- Tamaños ----------

    private fun cargarTamanos() {
        viewModel.tamanos.observe(this) { tamanos ->
            if (tamanos.isEmpty()) return@observe
            listaTamanos = tamanos
            // Seleccionar el primer tamaño por defecto
            precioTamanoSeleccionado = tamanos[0].precio
            btnSeleccionarTamano.text = tamanos[0].nombre
            actualizarPrecioTotal()
        }
    }

    private fun mostrarDialogoTamanos() {
        val opciones = listaTamanos.map { "${it.nombre}  —  $${"%,.0f".format(it.precio)}" }.toTypedArray()
        var seleccionado = listaTamanos.indexOfFirst { it.precio == precioTamanoSeleccionado }.takeIf { it >= 0 } ?: 0

        AlertDialog.Builder(this, R.style.DialogTamano)
            .setTitle("Selecciona el tamaño")
            .setSingleChoiceItems(opciones, seleccionado) { _, which ->
                seleccionado = which
            }
            .setPositiveButton("Aceptar") { _, _ ->
                val tamanoElegido = listaTamanos[seleccionado]
                precioTamanoSeleccionado = tamanoElegido.precio
                btnSeleccionarTamano.text = tamanoElegido.nombre
                actualizarPrecioTotal()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    // ---------- Sabores ----------

    private fun cargarSabores() {
        viewModel.sabores.observe(this) { sabores ->
            layoutSabores.removeAllViews()
            if (sabores.isEmpty()) return@observe

            val inflater = LayoutInflater.from(this)
            sabores.forEach { sabor ->
                val item = inflater.inflate(R.layout.item_sabor, layoutSabores, false)
                item.findViewById<TextView>(R.id.tvNombreSabor).text = sabor.nombre

                val checkbox = item.findViewById<android.widget.CheckBox>(R.id.checkboxSabor)
                checkbox.setOnCheckedChangeListener { _, _ -> }

                layoutSabores.addView(item)
            }
        }
    }

    // ---------- Adiciones ----------

    private fun cargarAdiciones() {
        viewModel.adiciones.observe(this) { adiciones ->
            layoutAdiciones.removeAllViews()
            if (adiciones.isEmpty()) return@observe

            val inflater = LayoutInflater.from(this)
            adiciones.forEach { adicion ->
                val item = inflater.inflate(R.layout.item_adicion, layoutAdiciones, false)

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

                layoutAdiciones.addView(item)
            }
        }
    }

    // ---------- Precio ----------

    private fun actualizarPrecioTotal() {
        // Precio = (tamaño + adiciones elegidas) × cantidad de platos
        val total = (precioTamanoSeleccionado + precioAdicionesTotal) * cantidad
        tvPrecioTotal.text = getString(R.string.precio_total_label, total.toInt())
    }
}