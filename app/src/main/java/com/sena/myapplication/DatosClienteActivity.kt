package com.sena.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class DatosClienteActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_datos_cliente)
        configurarBarraNavegacion()

        val etNombre   = findViewById<TextInputEditText>(R.id.etNombre)
        val etCedula   = findViewById<TextInputEditText>(R.id.etCedula)
        val etCorreo   = findViewById<TextInputEditText>(R.id.etCorreo)
        val etTelefono = findViewById<TextInputEditText>(R.id.etTelefono)
        val btnVolver      = findViewById<Button>(R.id.btnVolver)
        val btnContinuar   = findViewById<Button>(R.id.btnContinuar)

        // Recibir extras del carrito
        val carritoIdPlato = intent.getIntExtra("CARRITO_ID_PLATO", -1)
        val carritoNombre  = intent.getStringExtra("CARRITO_NOMBRE") ?: ""
        val carritoTamano  = intent.getStringExtra("CARRITO_TAMANO") ?: ""
        val carritoCantidad = intent.getIntExtra("CARRITO_CANTIDAD", 1)
        val carritoPrecio  = intent.getDoubleExtra("CARRITO_PRECIO_UNITARIO", 0.0)

        btnVolver.setOnClickListener { finish() }

        btnContinuar.setOnClickListener {
            val nombre   = etNombre.text.toString().trim()
            val cedula   = etCedula.text.toString().trim()
            val correo   = etCorreo.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()

            if (nombre.isEmpty() || cedula.isEmpty() || correo.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(this, "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(this, ReservaActivity::class.java).apply {
                // Datos del cliente
                putExtra("CLI_NOMBRE",   nombre)
                putExtra("CLI_CEDULA",   cedula)
                putExtra("CLI_CORREO",   correo)
                putExtra("CLI_TELEFONO", telefono)
                // Datos del carrito (para el pedido)
                putExtra("CARRITO_ID_PLATO",        carritoIdPlato)
                putExtra("CARRITO_NOMBRE",          carritoNombre)
                putExtra("CARRITO_TAMANO",          carritoTamano)
                putExtra("CARRITO_CANTIDAD",        carritoCantidad)
                putExtra("CARRITO_PRECIO_UNITARIO", carritoPrecio)
            }
            startActivity(intent)
        }
    }
}

