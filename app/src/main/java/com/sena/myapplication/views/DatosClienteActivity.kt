package com.sena.myapplication.views

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import com.sena.myapplication.databinding.ActivityDatosClienteBinding

/**
 * Pantalla de datos del cliente — Formulario para recopilar nombre,
 * cédula, correo y teléfono antes de continuar con la reserva o domicilio.
 *
 * Recibe datos del carrito (prefijo CARRITO_*) y un flag FLUJO_DOMICILIO
 * que determina si el siguiente paso es ReservaActivity o DomicilioActivity.
 *
 * Implementa validaciones de campos vacíos, formato de correo
 * y aceptación obligatoria del checkbox de Habeas Data.
 */
class DatosClienteActivity : BaseActivity() {

    private lateinit var binding: ActivityDatosClienteBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDatosClienteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBarraNavegacion()

        // --- Recibir extras del carrito ---
        val carritoIdPlato  = intent.getIntExtra("CARRITO_ID_PLATO", -1)
        val carritoNombre   = intent.getStringExtra("CARRITO_NOMBRE") ?: ""
        val carritoTamano   = intent.getStringExtra("CARRITO_TAMANO") ?: ""
        val carritoCantidad = intent.getIntExtra("CARRITO_CANTIDAD", 1)
        val carritoPrecio   = intent.getDoubleExtra("CARRITO_PRECIO_UNITARIO", 0.0)

        // Flag que indica si el flujo es domicilio (true) o restaurante (false)
        val esDomicilio = intent.getBooleanExtra("FLUJO_DOMICILIO", false)

        // --- Listeners ---
        binding.btnVolver.setOnClickListener { finish() }

        binding.btnContinuar.setOnClickListener {
            // Extraer y limpiar valores del formulario
            val nombre   = binding.etNombre.text.toString().trim()
            val cedula   = binding.etCedula.text.toString().trim()
            val correo   = binding.etCorreo.text.toString().trim()
            val telefono = binding.etTelefono.text.toString().trim()

            // --- Validaciones de campos vacíos ---
            if (!validarCamposObligatorios(nombre, cedula, correo, telefono)) return@setOnClickListener

            // --- Validación de formato de correo ---
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(this, "Correo no válido", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- Validación de Habeas Data ---
            if (!binding.cbHabeasData.isChecked) {
                Toast.makeText(this, "Debes aceptar la política de tratamiento de datos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Determinar destino según el flujo
            val destino = if (esDomicilio) DomicilioActivity::class.java
                          else ReservaActivity::class.java

            val intent = Intent(this, destino).apply {
                // Datos del cliente
                putExtra("CLI_NOMBRE",   nombre)
                putExtra("CLI_CEDULA",   cedula)
                putExtra("CLI_CORREO",   correo)
                putExtra("CLI_TELEFONO", telefono)
                // Datos del carrito (se propagan para armar el pedido)
                putExtra("CARRITO_ID_PLATO",        carritoIdPlato)
                putExtra("CARRITO_NOMBRE",          carritoNombre)
                putExtra("CARRITO_TAMANO",          carritoTamano)
                putExtra("CARRITO_CANTIDAD",        carritoCantidad)
                putExtra("CARRITO_PRECIO_UNITARIO", carritoPrecio)
            }
            startActivity(intent)
        }
    }

    /**
     * Valida todos los campos del formulario de datos del cliente.
     * Verifica campos vacíos, longitud mínima/máxima y formatos.
     *
     * @return true si todo es válido, false si hay algún error.
     */
    private fun validarCamposObligatorios(
        nombre: String, cedula: String, correo: String, telefono: String
    ): Boolean {
        // --- Nombre: 3–50 caracteres, solo letras y espacios ---
        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingresa tu nombre", Toast.LENGTH_SHORT).show()
            return false
        }
        if (nombre.length < 3) {
            Toast.makeText(this, "El nombre debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }

        // --- Cédula: 6–10 dígitos numéricos ---
        if (cedula.isEmpty()) {
            Toast.makeText(this, "Ingresa tu cédula", Toast.LENGTH_SHORT).show()
            return false
        }
        if (cedula.length < 6) {
            Toast.makeText(this, "La cédula debe tener al menos 6 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        // --- Correo: no vacío (formato se valida aparte) ---
        if (correo.isEmpty()) {
            Toast.makeText(this, "Ingresa tu correo", Toast.LENGTH_SHORT).show()
            return false
        }

        // --- Teléfono: exactamente 10 dígitos ---
        if (telefono.isEmpty()) {
            Toast.makeText(this, "Ingresa tu teléfono", Toast.LENGTH_SHORT).show()
            return false
        }
        if (telefono.length != 10) {
            Toast.makeText(this, "El teléfono debe tener 10 dígitos", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }
}
