package com.sena.myapplication

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView

class ConfirmacionReservaActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirmacion_reserva)
        configurarBarraNavegacion()

        val imgQr       = findViewById<ImageView>(R.id.imgQr)
        val btnFinalizar = findViewById<Button>(R.id.btnFinalizar)

        // Recibir QR en base64 desde ReservaActivity
        val qrBase64 = intent.getStringExtra("QR_BASE64") ?: ""

        if (qrBase64.isNotEmpty()) {
            try {
                val bytes  = Base64.decode(qrBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imgQr.setImageBitmap(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        btnFinalizar.setOnClickListener {
            // Volver al inicio limpiando toda la pila
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}

