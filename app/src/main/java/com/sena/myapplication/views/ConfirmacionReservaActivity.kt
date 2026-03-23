package com.sena.myapplication.views

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import com.sena.myapplication.databinding.ActivityConfirmacionReservaBinding

/**
 * Pantalla de confirmación — Muestra el código QR de la reserva creada.
 *
 * Recibe el QR en formato Base64 vía Intent extra "QR_BASE64".
 * El botón "Finalizar" limpia la pila de Activities y vuelve al inicio.
 *
 * Nota: Esta Activity extiende BaseActivity para mantener consistencia
 * con la barra de navegación, aunque el flujo normalmente termina aquí.
 */
class ConfirmacionReservaActivity : BaseActivity() {

    private lateinit var binding: ActivityConfirmacionReservaBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfirmacionReservaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBarraNavegacion()

        // --- Decodificar y mostrar el QR en Base64 ---
        val qrBase64 = intent.getStringExtra("QR_BASE64") ?: ""

        if (qrBase64.isNotEmpty()) {
            try {
                val bytes  = Base64.decode(qrBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.imgQr.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // --- Botón Finalizar: Volver al inicio limpiando toda la pila ---
        binding.btnFinalizar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}

