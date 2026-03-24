package com.sena.myapplication.views

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sena.myapplication.R

/**
 * Clase base para todas las Activities de la aplicación.
 *
 * Principio OCP (Open/Closed): Las Activities hijas extienden esta clase
 * para heredar la barra de navegación y utilidades compartidas sin modificarla.
 *
 * Principio DRY: Centraliza la lógica de navegación inferior, el diálogo
 * selector genérico y utilidades de archivos/imágenes que comparten
 * múltiples pantallas (ReservaActivity, DomicilioActivity, MisReservasActivity).
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

        // Botón Mis Reservas → Abrir MisReservasActivity
        btnNotificacion.setOnClickListener {
            startActivity(Intent(this, MisReservasActivity::class.java))
        }
    }

    // ==================== DIÁLOGO SELECTOR GENÉRICO ====================

    /**
     * Muestra un diálogo personalizado con una lista de opciones y selección visual.
     * Reutilizado por todos los selectores (bloque horario, piso, método de pago,
     * temática) en múltiples Activities.
     *
     * Principio DRY: Una sola implementación para todos los diálogos de selección
     * en la aplicación, evitando duplicación en Activities hijas.
     *
     * @param titulo Título del diálogo.
     * @param opciones Lista de textos a mostrar como opciones.
     * @param indiceActual Índice actualmente seleccionado (-1 si ninguno).
     * @param alSeleccionar Lambda que recibe el índice y texto seleccionados.
     */
    protected fun mostrarDialogoSelector(
        titulo: String,
        opciones: List<String>,
        indiceActual: Int,
        alSeleccionar: (Int, String) -> Unit
    ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_selector_generico)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.findViewById<TextView>(R.id.tvTituloDialogo).text = titulo
        val contenedor = dialog.findViewById<LinearLayout>(R.id.contenedorOpciones)

        // Si no hay selección previa, elegir el primer elemento por defecto
        var indiceTemp = if (indiceActual == -1 && opciones.isNotEmpty()) 0 else indiceActual
        val vistasFilas = mutableListOf<View>()

        for (i in opciones.indices) {
            val vistaFila = LayoutInflater.from(this)
                .inflate(R.layout.item_opcion_dialogo, contenedor, false)
            val tvTexto  = vistaFila.findViewById<TextView>(R.id.tvTextoOpcion)
            val imgCheck = vistaFila.findViewById<ImageButton>(R.id.imgCheckOpcion)

            tvTexto.text = opciones[i]

            // Estilo visual de selección inicial
            if (i == indiceTemp) {
                imgCheck.setImageResource(R.drawable.ic_radio_seleccionado)
                tvTexto.setTextColor(Color.parseColor("#FFFFFF"))
            } else {
                imgCheck.setImageResource(R.drawable.ic_radio_vacio)
                tvTexto.setTextColor(Color.parseColor("#99FFFFFF"))
            }

            vistaFila.setOnClickListener {
                indiceTemp = i
                vistasFilas.forEachIndexed { index, vista ->
                    val img = vista.findViewById<ImageButton>(R.id.imgCheckOpcion)
                    val txt = vista.findViewById<TextView>(R.id.tvTextoOpcion)

                    if (index == indiceTemp) {
                        img.setImageResource(R.drawable.ic_radio_seleccionado)
                        txt.setTextColor(Color.parseColor("#FFFFFF"))
                    } else {
                        img.setImageResource(R.drawable.ic_radio_vacio)
                        txt.setTextColor(Color.parseColor("#99FFFFFF"))
                    }
                }
            }

            contenedor.addView(vistaFila)
            vistasFilas.add(vistaFila)
        }

        dialog.findViewById<Button>(R.id.btnCancelarDialogo).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.btnAceptarDialogo).setOnClickListener {
            if (indiceTemp != -1) alSeleccionar(indiceTemp, opciones[indiceTemp])
            dialog.dismiss()
        }
        dialog.show()
    }

    // ==================== UTILIDADES DE ARCHIVOS ====================

    /**
     * Codifica una imagen desde su URI a una cadena Base64.
     * Lee los bytes a través del ContentResolver y los convierte.
     *
     * Principio SRP: Método utilitario puro — solo transforma URI → Base64.
     *
     * @param uri URI de la imagen a codificar.
     * @return String en Base64 o null si ocurre un error de lectura.
     */
    protected fun codificarImagenBase64(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val bytes = inputStream.readBytes()
            inputStream.close()
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Obtiene el nombre legible del archivo desde una URI de contenido.
     * Intenta extraerlo del cursor de MediaStore; si no, retorna un texto genérico.
     *
     * @param uri URI del archivo seleccionado.
     * @return Nombre del archivo o "Comprobante seleccionado" como fallback.
     */
    protected fun obtenerNombreArchivo(uri: Uri): String {
        var nombre = "Comprobante seleccionado"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (index != -1 && cursor.moveToFirst()) {
                nombre = cursor.getString(index)
            }
        }
        return nombre
    }
}

