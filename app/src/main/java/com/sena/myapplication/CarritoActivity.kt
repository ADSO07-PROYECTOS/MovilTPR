package com.sena.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class CarritoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_carrito)
        configurarBarraNavegacion()

        // Recibir datos del plato
        val idPlato      = intent.getIntExtra("CARRITO_ID_PLATO", -1)
        val nombrePlato  = intent.getStringExtra("CARRITO_NOMBRE")    ?: ""
        val nombreTamano = intent.getStringExtra("CARRITO_TAMANO")    ?: ""
        val cantidad     = intent.getIntExtra("CARRITO_CANTIDAD", 1)
        val precioUnitario = intent.getDoubleExtra("CARRITO_PRECIO_UNITARIO", 0.0)

        // Vincular vistas
        val tvNombrePlato   = findViewById<TextView>(R.id.tvCarritoNombrePlato)
        val tvTamano        = findViewById<TextView>(R.id.tvCarritoTamano)
        val tvCantidad      = findViewById<TextView>(R.id.tvCarritoCantidad)
        val tvPrecioItem    = findViewById<TextView>(R.id.tvCarritoPrecioItem)
        val tvTotal         = findViewById<TextView>(R.id.tvCarritoTotal)
        val tvBtnPagar      = findViewById<Button>(R.id.btnIrAPagar)
        val btnAtras        = findViewById<ImageButton>(R.id.btnCarritoAtras)
        val btnMenos        = findViewById<ImageButton>(R.id.btnCarritoMenos)
        val btnMas          = findViewById<ImageButton>(R.id.btnCarritoMas)

        var cantidadActual = cantidad
        val precioUnitarioActual = precioUnitario

        fun actualizarUI() {
            tvCantidad.text = cantidadActual.toString()
            val precioTotalActual = precioUnitarioActual * cantidadActual
            val totalFormato = "$ %,.0f".format(precioTotalActual)
            tvPrecioItem.text = totalFormato
            tvTotal.text = getString(R.string.carrito_total, totalFormato)
            tvBtnPagar.text = getString(R.string.carrito_ir_pagar, totalFormato)
        }

        // Asignar datos iniciales
        tvNombrePlato.text = nombrePlato.uppercase()
        tvTamano.text      = nombreTamano.uppercase()
        actualizarUI()

        btnMenos.setOnClickListener {
            if (cantidadActual > 1) {
                cantidadActual--
                actualizarUI()
            }
        }

        btnMas.setOnClickListener {
            cantidadActual++
            actualizarUI()
        }

        btnAtras.setOnClickListener { finish() }

        tvBtnPagar.setOnClickListener {
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

        dialogView.findViewById<Button>(R.id.btnDomicilio).setOnClickListener {
            dialog.dismiss()
            // TODO: flujo domicilio
        }

        dialogView.findViewById<TextView>(R.id.btnCancelar).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }
}
