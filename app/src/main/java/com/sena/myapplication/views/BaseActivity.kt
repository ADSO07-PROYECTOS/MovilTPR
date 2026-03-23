package com.sena.myapplication.views

import android.content.Intent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.sena.myapplication.R

/**
 * Clase base para todas las Activities de la aplicación.
 *
 * Principio OCP (Open/Closed): Las Activities hijas extienden esta clase
 * para heredar la barra de navegación sin modificarla.
 *
 * Principio DRY: Centraliza la lógica de navegación inferior que comparten
 * todas las pantallas.
 *
 * Uso: Llamar a [configurarBarraNavegacion] después de [setContentView].
 */
open class BaseActivity : AppCompatActivity() {

    /**
     * Vincula los botones de la barra de navegación inferior y define
     * su comportamiento. Debe llamarse después de [setContentView].
     *
     * Si algún botón no existe en el layout actual, la función
     * retorna sin error (protección con safe-call ?:).
     */
    protected fun configurarBarraNavegacion() {
        val btnCasa         = findViewById<ImageButton?>(R.id.btnCasa)         ?: return
        val btnCarrito      = findViewById<ImageButton?>(R.id.btnCarrito)      ?: return
        val btnNotificacion = findViewById<ImageButton?>(R.id.btnNotification) ?: return

        // Botón Casa → Volver a MainActivity limpiando la pila
        btnCasa.setOnClickListener {
            startActivity(
                Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                }
            )
        }

        // Botón Carrito → Abrir CarritoActivity
        btnCarrito.setOnClickListener {
            startActivity(Intent(this, CarritoActivity::class.java))
        }

        // Botón Notificación → Pendiente de implementar
        btnNotificacion.setOnClickListener { /* TODO: Pantalla de reservas activas */ }
    }
}

