package com.sena.myapplication.views

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.sena.myapplication.R
import com.sena.myapplication.databinding.ActivityCarritoBinding


class CarritoActivity : BaseActivity() {

    private lateinit var binding: ActivityCarritoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCarritoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBarraNavegacion()

        // --- Recibir datos del plato desde el Intent ---
        val idPlato        = intent.getIntExtra("CARRITO_ID_PLATO", -1)
        val nombrePlato    = intent.getStringExtra("CARRITO_NOMBRE")    ?: ""
        val nombreTamano   = intent.getStringExtra("CARRITO_TAMANO")    ?: ""
        val cantidad       = intent.getIntExtra("CARRITO_CANTIDAD", 1)
        val precioUnitario = intent.getDoubleExtra("CARRITO_PRECIO_UNITARIO", 0.0)

        // --- Si no hay plato en el carrito, mostrar estado vacío ---
        if (idPlato == -1) {
            binding.cardPlato.visibility = android.view.View.GONE
            binding.layoutMetodoPago.visibility = android.view.View.GONE
            binding.tvCarritoTotal.visibility = android.view.View.GONE
            binding.btnIrAPagar.visibility = android.view.View.GONE
            binding.tvCarritoVacio.visibility = android.view.View.VISIBLE
            binding.btnCarritoAtras.setOnClickListener { finish() }
            return
        }

        var cantidadActual = cantidad
        val precioUnitarioActual = precioUnitario

        // --- Función local para actualizar toda la UI ---
        fun actualizarUI() {
            binding.tvCarritoCantidad.text = cantidadActual.toString()
            val precioTotalActual = precioUnitarioActual * cantidadActual
            val totalFormato = "$ %,.0f".format(precioTotalActual)
            binding.tvCarritoPrecioItem.text = totalFormato
            binding.tvCarritoTotal.text = getString(R.string.carrito_total, totalFormato)
            binding.btnIrAPagar.text = getString(R.string.carrito_ir_pagar, totalFormato)
        }

        // --- Datos iniciales ---
        binding.tvCarritoNombrePlato.text = nombrePlato.uppercase()
        binding.tvCarritoTamano.text      = nombreTamano.uppercase()
        actualizarUI()

        // --- Listeners de botones ---
        binding.btnCarritoMenos.setOnClickListener {
            if (cantidadActual > 1) {
                cantidadActual--
                actualizarUI()
            }
        }

        binding.btnCarritoMas.setOnClickListener {
            cantidadActual++
            actualizarUI()
        }

        binding.btnCarritoAtras.setOnClickListener { finish() }

        binding.btnIrAPagar.setOnClickListener {
            mostrarDialogoPedido(
                idPlato      = idPlato,
                nombrePlato  = nombrePlato,
                nombreTamano = nombreTamano,
                cantidad     = cantidadActual,
                precio       = precioUnitarioActual
            )
        }
    }

    private fun mostrarDialogoPedido(
        idPlato: Int, nombrePlato: String, nombreTamano: String,
        cantidad: Int, precio: Double
    ) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_tipo_pedido, null)
        val dialog = AlertDialog.Builder(this, R.style.DialogTamano)
            .setView(dialogView)
            .create()

        // Opción: Reservar en restaurante → Navegar a datos del cliente
        dialogView.findViewById<Button>(R.id.btnReservarRestaurante).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, DatosClienteActivity::class.java).apply {
                putExtra("CARRITO_ID_PLATO",        idPlato)
                putExtra("CARRITO_NOMBRE",          nombrePlato)
                putExtra("CARRITO_TAMANO",          nombreTamano)
                putExtra("CARRITO_CANTIDAD",        cantidad)
                putExtra("CARRITO_PRECIO_UNITARIO", precio)
            }
            startActivity(intent)
        }

        // Opción: Domicilio → Navegar a datos del cliente con flag domicilio
        dialogView.findViewById<Button>(R.id.btnDomicilio).setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, DatosClienteActivity::class.java).apply {
                putExtra("CARRITO_ID_PLATO",        idPlato)
                putExtra("CARRITO_NOMBRE",          nombrePlato)
                putExtra("CARRITO_TAMANO",          nombreTamano)
                putExtra("CARRITO_CANTIDAD",        cantidad)
                putExtra("CARRITO_PRECIO_UNITARIO", precio)
                putExtra("FLUJO_DOMICILIO",         true)
            }
            startActivity(intent)
        }

        dialogView.findViewById<TextView>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}

