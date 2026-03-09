
package com.sena.myapplication

import android.content.Intent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {

    /**
     * Vincula los botones de la barra de navegación inferior y define
     * su comportamiento. Debe llamarse después de [setContentView].
     */
    protected fun configurarBarraNavegacion() {
        val btnCasa         = findViewById<ImageButton?>(R.id.btnCasa)         ?: return
        val btnCarrito      = findViewById<ImageButton?>(R.id.btnCarrito)      ?: return
        val btnNotificacion = findViewById<ImageButton?>(R.id.btnNotification) ?: return

        btnCasa.setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
        }

        btnCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        btnNotificacion.setOnClickListener { /* Pendiente: pantalla de reservas */ }
    }
}
