package com.sena.myapplication.views

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import com.sena.myapplication.databinding.ActivityConfirmacionReservaBinding
import java.io.File
import java.io.FileOutputStream


class ConfirmacionReservaActivity : BaseActivity() {

    private lateinit var binding: ActivityConfirmacionReservaBinding
    private var qrBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityConfirmacionReservaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBarraNavegacion()

        // --- Decodificar y mostrar el QR en Base64 ---
        val qrBase64 = intent.getStringExtra("QR_BASE64") ?: ""

        if (qrBase64.isNotEmpty()) {
            try {
                val bytes = Base64.decode(qrBase64, Base64.DEFAULT)
                qrBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.imgQr.setImageBitmap(qrBitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // --- Botón Descargar QR ---
        binding.btnDescargarQr.setOnClickListener {
            guardarQrEnGaleria()
        }

        // --- Botón Finalizar: Volver al inicio limpiando toda la pila ---
        binding.btnFinalizar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }


    private fun guardarQrEnGaleria() {
        val bitmap = qrBitmap
        if (bitmap == null) {
            Toast.makeText(this, "No hay código QR para descargar", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val nombreArchivo = "QR_Pedido_${System.currentTimeMillis()}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ → MediaStore (sin permisos)
                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, nombreArchivo)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SaboresUnidos")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
                )

                uri?.let {
                    contentResolver.openOutputStream(it)?.use { outputStream ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                    }
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(it, contentValues, null, null)

                    Toast.makeText(this, "QR guardado en Galería/SaboresUnidos", Toast.LENGTH_LONG).show()
                } ?: run {
                    Toast.makeText(this, "Error al guardar el QR", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Android 9 y anteriores → Escritura directa
                val directorio = File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                    "SaboresUnidos"
                )
                if (!directorio.exists()) directorio.mkdirs()

                val archivo = File(directorio, nombreArchivo)
                FileOutputStream(archivo).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                // Notificar a la galería
                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                mediaScanIntent.data = android.net.Uri.fromFile(archivo)
                sendBroadcast(mediaScanIntent)

                Toast.makeText(this, "QR guardado en Pictures/SaboresUnidos", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al descargar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

