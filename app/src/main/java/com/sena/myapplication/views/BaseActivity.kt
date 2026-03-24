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
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

open class BaseActivity : AppCompatActivity() {


    protected fun configurarBarraNavegacion() {
      val btnCasa         = findViewById<ImageButton?>(R.id.btnCasa)         ?: return
      val btnCarrito      = findViewById<ImageButton?>(R.id.btnCarrito)      ?: return
      val btnNotificacion = findViewById<ImageButton?>(R.id.btnNotification) ?: return

      val barraInferior = findViewById<View>(R.id.barraInferior)
      if (barraInferior != null) {
        ViewCompat.setOnApplyWindowInsetsListener(barraInferior) { view, insets ->
          val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

          val params = view.layoutParams as ViewGroup.MarginLayoutParams
          params.bottomMargin = systemBars.bottom
          view.layoutParams = params

          insets
        }
      }
      // -----------------------------------------------------------

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

      btnNotificacion.setOnClickListener {
        startActivity(Intent(this, MisReservasActivity::class.java))
      }
    }

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
                tvTexto.setTextColor(Color.parseColor("#CCF5F5F5"))
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
                        txt.setTextColor(Color.parseColor("#CCF5F5F5"))
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

