package com.sena.myapplication.views

import android.app.DatePickerDialog
import android.app.Dialog
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.textfield.TextInputEditText
import com.sena.myapplication.R
import com.sena.myapplication.databinding.ActivityMisReservasBinding
import com.sena.myapplication.models.ActualizarReservaRequest
import com.sena.myapplication.models.ModelMiReserva
import com.sena.myapplication.models.ModelTematica
import com.sena.myapplication.viewmodels.ViewModelMisReservas
import java.util.Calendar
import java.util.Locale

/**
 * Pantalla "Mis Reservas" — Permite buscar reservas por cédula,
 * ver los detalles con QR, editar y eliminar.
 *
 * Principio SRP: Solo gestiona la UI de consulta/edición/eliminación.
 * Las operaciones de red se delegan a [ViewModelMisReservas] y [ViewModelReserva].
 *
 * Principio OCP: Hereda [mostrarDialogoSelector] de [BaseActivity]
 * para los selectores del diálogo de edición, sin reimplementarlo.
 *
 * Principio DIP: Depende de las abstracciones [ViewModelMisReservas]
 * y [ViewModelReserva], no de llamadas Retrofit directas.
 */
class MisReservasActivity : BaseActivity() {

    private lateinit var binding: ActivityMisReservasBinding
    private lateinit var viewModel: ViewModelMisReservas

    private var reservaActual: ModelMiReserva? = null
    private var listaTematicas: List<ModelTematica> = emptyList()
    private var cedulaActual: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMisReservasBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBarraNavegacion()

        viewModel = ViewModelProvider(this)[ViewModelMisReservas::class.java]

        // Cargar temáticas desde el mismo microservicio (puerto 5007)
        viewModel.cargarTematicas()
        viewModel.tematicas.observe(this) { listaTematicas = it }

        // --- Observar resultados ---

        viewModel.reservas.observe(this) { reservas ->
            if (reservas.isNotEmpty()) {
                reservaActual = reservas[0]
                mostrarReserva(reservas[0])
            } else {
                ocultarReserva()
            }
        }

        viewModel.error.observe(this) { msg ->
            msg?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
        }

        viewModel.cargando.observe(this) { cargando ->
            binding.progressBar.visibility = if (cargando) View.VISIBLE else View.GONE
        }

        viewModel.actualizacionExitosa.observe(this) { resp ->
            resp?.let {
                Toast.makeText(this, "Reserva actualizada exitosamente", Toast.LENGTH_SHORT).show()
                // Recargar
                if (cedulaActual.isNotEmpty()) viewModel.buscarPorCedula(cedulaActual)
            }
        }

        viewModel.eliminacionExitosa.observe(this) { resp ->
            resp?.let {
                Toast.makeText(this, "Reserva eliminada exitosamente", Toast.LENGTH_SHORT).show()
                reservaActual = null
                ocultarReserva()
                binding.tvSinReservas.text = "Reserva eliminada"
            }
        }

        // --- Listeners ---

        binding.btnVolver.setOnClickListener { finish() }

        binding.btnBuscar.setOnClickListener {
            val cedula = binding.etCedulaBuscar.text.toString().trim()
            if (cedula.length < 6) {
                Toast.makeText(this, "Ingresa una cédula válida (mín. 6 dígitos)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            cedulaActual = cedula
            viewModel.buscarPorCedula(cedula)
        }

        binding.btnEditar.setOnClickListener {
            reservaActual?.let { mostrarDialogoEditar(it) }
        }

        binding.btnEliminar.setOnClickListener {
            reservaActual?.let { reserva ->
                AlertDialog.Builder(this, R.style.DialogTamano)
                    .setTitle("Eliminar reserva")
                    .setMessage("¿Estás seguro de que deseas eliminar esta reserva? Esta acción no se puede deshacer.")
                    .setPositiveButton("Eliminar") { _, _ ->
                        viewModel.eliminarReserva(reserva.reservaId)
                    }
                    .setNegativeButton("Cancelar", null)
                    .show()
            }
        }
    }

    // ==================== MOSTRAR RESERVA ====================

    private fun mostrarReserva(reserva: ModelMiReserva) {
        binding.scrollContenido.visibility = View.VISIBLE
        binding.tvSinReservas.visibility = View.GONE

        // Usar fecha_formato que ya viene formateada del backend
        val fechaDisplay = reserva.fechaFormato.ifEmpty { reserva.fechaHora }
        binding.tvFechaHora.text = fechaDisplay

        binding.tvMesaPersonas.text = "MESA PARA ${reserva.personas}"
        binding.tvTematica.text = if (reserva.tematica.isNotEmpty()) reserva.tematica.uppercase() else "SIN TEMÁTICA"
        binding.tvPiso.text = if (reserva.piso > 0) "PISO ${reserva.piso}" else ""

        // Pedido
        val pedidoTexto = reserva.pedido?.joinToString("\n") { item ->
            if (item.cantidad > 1) "${item.nombre} x${item.cantidad}" else item.nombre
        }
        binding.tvPedido.text = if (pedidoTexto.isNullOrEmpty()) "Sin pedido asociado" else pedidoTexto

        // QR (campo qr_b64 del nuevo microservicio)
        val qrBase64 = reserva.qr
        if (!qrBase64.isNullOrEmpty()) {
            try {
                val bytes = Base64.decode(qrBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                binding.imgQrReserva.setImageBitmap(bitmap)
                binding.imgQrReserva.visibility = View.VISIBLE
            } catch (e: Exception) {
                binding.imgQrReserva.visibility = View.GONE
            }
        } else {
            binding.imgQrReserva.visibility = View.GONE
        }
    }

    private fun ocultarReserva() {
        binding.scrollContenido.visibility = View.GONE
        binding.tvSinReservas.visibility = View.VISIBLE
        binding.tvSinReservas.text = "No se encontraron reservas"
    }

    // ==================== DIÁLOGO EDITAR ====================

    private fun mostrarDialogoEditar(reserva: ModelMiReserva) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_editar_reserva)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.85f)

        val btnFecha = dialog.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnEditFecha)
        val btnHora = dialog.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnEditHora)
        val btnTematica = dialog.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnEditTematica)
        val etPersonas = dialog.findViewById<TextInputEditText>(R.id.etEditPersonas)
        val btnPiso = dialog.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.btnEditPiso)
        val btnActualizar = dialog.findViewById<Button>(R.id.btnActualizarReserva)

        // Extraer fecha y hora desde fecha_hora ("2026-03-24 07:00:00")
        var fechaSel = reserva.extraerFecha()
        var horaSel = reserva.extraerHora()
        var tematicaIdSel = reserva.tematicaId
        var pisoSel = if (reserva.piso > 0) reserva.piso else 1

        btnFecha.text = fechaSel
        btnHora.text = "$horaSel:00"
        btnTematica.text = reserva.tematica
        etPersonas.setText(reserva.personas.toString())
        btnPiso.text = "Piso ${reserva.piso}"

        // Selector de fecha
        btnFecha.setOnClickListener {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, 1)
            val picker = DatePickerDialog(this, R.style.CalendarioPersonalizado, { _, y, m, d ->
                val mes = String.format(Locale.US, "%02d", m + 1)
                val dia = String.format(Locale.US, "%02d", d)
                fechaSel = "$y-$mes-$dia"
                btnFecha.text = fechaSel
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
            picker.datePicker.minDate = cal.timeInMillis
            picker.show()
        }

        // Selector de hora (usa mostrarDialogoSelector heredado de BaseActivity)
        val opcionesHora = (6..22).map { "$it:00" }
        btnHora.setOnClickListener {
            mostrarDialogoSelector("Bloque Horario", opcionesHora,
                opcionesHora.indexOfFirst { it.startsWith(horaSel) }.takeIf { it >= 0 } ?: 0
            ) { _, texto ->
                horaSel = texto.replace(":00", "")
                btnHora.text = texto
            }
        }

        // Selector de temática
        btnTematica.setOnClickListener {
            if (listaTematicas.isEmpty()) {
                Toast.makeText(this, "Cargando temáticas...", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val nombres = listaTematicas.map { it.nombre }
            val idx = listaTematicas.indexOfFirst { it.id == tematicaIdSel }.takeIf { it >= 0 } ?: 0
            mostrarDialogoSelector("Temática", nombres, idx) { pos, texto ->
                tematicaIdSel = listaTematicas[pos].id
                btnTematica.text = texto
            }
        }

        // Selector de piso
        val opcionesPiso = listOf("Piso 1", "Piso 2")
        btnPiso.setOnClickListener {
            mostrarDialogoSelector("Seleccionar Piso", opcionesPiso, pisoSel - 1) { pos, texto ->
                pisoSel = pos + 1
                btnPiso.text = texto
            }
        }

        // Actualizar
        btnActualizar.setOnClickListener {
            val personas = etPersonas.text.toString().trim().toIntOrNull() ?: 0
            if (personas <= 0 || personas > 12) {
                Toast.makeText(this, "Personas debe ser entre 1 y 12", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Construir fecha_hora combinada: "YYYY-MM-DD HH:00:00"
            val horaFormateada = String.format(Locale.US, "%02d", horaSel.toIntOrNull() ?: 0)
            val fechaHoraCombinada = "$fechaSel $horaFormateada:00:00"

            val body = ActualizarReservaRequest(
                fechaHora = fechaHoraCombinada,
                personas = personas,
                tematicaId = tematicaIdSel,
                nombre = reserva.nombreCliente,
                email = reserva.email,
                telefono = reserva.telefono
            )
            viewModel.actualizarReserva(reserva.reservaId, body)
            dialog.dismiss()
        }

        dialog.show()
    }

}

