package com.sena.myapplication.views

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.sena.myapplication.R
import com.sena.myapplication.databinding.ActivityReservaBinding
import com.sena.myapplication.models.*
import com.sena.myapplication.viewmodels.ViewModelReserva
import java.util.Calendar

/**
 * Pantalla de reserva — El usuario selecciona fecha, hora, temática,
 * piso, método de pago y número de personas.
 *
 * Patrón MVVM: Toda la lógica de red (cargar temáticas, enviar reserva)
 * se delega a [ViewModelReserva]. La Activity solo gestiona la UI.
 *
 * Recibe datos del cliente (CLI_*) y del carrito (CARRITO_*) para
 * construir el [ReservaRequest] completo.
 */
class ReservaActivity : BaseActivity() {

    private lateinit var binding: ActivityReservaBinding
    private lateinit var viewModel: ViewModelReserva

    // Estado de selección de la reserva
    private var listaTematicas: List<ModelTematica> = emptyList()
    private var fechaSeleccionada = ""
    private var bloqueHoraStr = ""
    private var bloquePos = -1
    private var tematicaSeleccionadaId = -1
    private var tematicaPos = -1
    private var pisoNumInt = -1
    private var pisoPos = -1
    private var metodoPagoStr = ""
    private var metodoPos = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReservaBinding.inflate(layoutInflater)
        setContentView(binding.root)
        configurarBarraNavegacion()

        // --- Recibir datos del Intent ---
        val cliNombre   = intent.getStringExtra("CLI_NOMBRE") ?: ""
        val cliCedula   = intent.getStringExtra("CLI_CEDULA") ?: ""
        val cliCorreo   = intent.getStringExtra("CLI_CORREO") ?: ""
        val cliTelefono = intent.getStringExtra("CLI_TELEFONO") ?: ""

        val carritoIdPlato  = intent.getIntExtra("CARRITO_ID_PLATO", -1)
        val carritoCantidad = intent.getIntExtra("CARRITO_CANTIDAD", 1)
        val carritoPrecio   = intent.getDoubleExtra("CARRITO_PRECIO_UNITARIO", 0.0)

        // --- Opciones de selección ---
        val opcionesBloque = (6..22).map { "$it:00" }
        val opcionesPiso   = listOf("Piso 1", "Piso 2")
        val opcionesMetodo = listOf("Efectivo", "Transferencia")

        // --- Inicializar ViewModel ---
        viewModel = ViewModelProvider(this)[ViewModelReserva::class.java]

        // Observar temáticas cargadas
        viewModel.tematicas.observe(this) { tematicas ->
            listaTematicas = tematicas
            binding.btnTematica.text = "Seleccionar Temática"
            binding.btnTematica.isEnabled = true
        }

        // Observar resultado de crear reserva → navegar a confirmación
        viewModel.reservaExitosa.observe(this) { response ->
            response?.let {
                val intent = Intent(this, ConfirmacionReservaActivity::class.java)
                intent.putExtra("QR_BASE64", it.qr ?: "")
                startActivity(intent)
                finishAffinity()
            }
        }

        // Observar errores de red
        viewModel.error.observe(this) { mensajeError ->
            mensajeError?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Observar estado de envío para habilitar/deshabilitar botón
        viewModel.enviando.observe(this) { enviando ->
            binding.btnReservar.isEnabled = !enviando
            binding.btnReservar.text = if (enviando) "Enviando..." else "RESERVAR"
        }

        // Disparar carga de temáticas
        viewModel.cargarTematicas()

        // --- Listeners de selección ---

        binding.btnFecha.setOnClickListener {
            val manana = Calendar.getInstance()
            manana.add(Calendar.DAY_OF_YEAR, 1)

            val picker = DatePickerDialog(
                this, R.style.CalendarioPersonalizado, { _, y, m, d ->
                    val mes = String.format("%02d", m + 1)
                    val dia = String.format("%02d", d)
                    fechaSeleccionada = "$y-$mes-$dia"
                    binding.btnFecha.text = fechaSeleccionada
                }, manana.get(Calendar.YEAR), manana.get(Calendar.MONTH), manana.get(Calendar.DAY_OF_MONTH)
            )
            picker.datePicker.minDate = manana.timeInMillis
            picker.show()
        }

        binding.btnBloque.setOnClickListener {
            mostrarDialogoSelector("Bloque Horario", opcionesBloque, bloquePos) { pos, texto ->
                bloquePos = pos
                bloqueHoraStr = texto.replace(":00", "")
                binding.btnBloque.text = texto
            }
        }

        binding.btnPiso.setOnClickListener {
            mostrarDialogoSelector("Seleccionar Piso", opcionesPiso, pisoPos) { pos, texto ->
                pisoPos = pos
                pisoNumInt = pos + 1
                binding.btnPiso.text = texto
            }
        }

        binding.btnMetodoPago.setOnClickListener {
            mostrarDialogoSelector("Método de Pago", opcionesMetodo, metodoPos) { pos, texto ->
                metodoPos = pos
                metodoPagoStr = texto.lowercase()
                binding.btnMetodoPago.text = texto
            }
        }

        binding.btnTematica.setOnClickListener {
            val nombres = listaTematicas.map { it.nombre }
            mostrarDialogoSelector("Temática", nombres, tematicaPos) { pos, texto ->
                tematicaPos = pos
                tematicaSeleccionadaId = listaTematicas[pos].id
                binding.btnTematica.text = texto
            }
        }

        // --- Botón Volver ---
        binding.btnVolverReserva.setOnClickListener { finish() }

        // --- Botón Reservar: Validar y enviar ---
        binding.btnReservar.setOnClickListener {
            val personas = binding.etPersonas.text.toString().trim().toIntOrNull() ?: 0

            // Validaciones estrictas de todos los campos
            if (!validarFormularioReserva(personas)) return@setOnClickListener

            // Construir el body del request
            val body = ReservaRequest(
                cliente = ClienteReserva(
                    doc = cliCedula,
                    nom = cliNombre,
                    correo = cliCorreo,
                    tel = cliTelefono
                ),
                reserva = DatosReserva(
                    fec = fechaSeleccionada,
                    hor = bloqueHoraStr,
                    tematica = tematicaSeleccionadaId,
                    personas = personas,
                    piso = pisoNumInt,
                    metodoPago = metodoPagoStr
                ),
                pedido = if (carritoIdPlato != -1) listOf(
                    PedidoItem(
                        id = carritoIdPlato,
                        cantidad = carritoCantidad,
                        precio = carritoPrecio
                    )
                ) else emptyList()
            )

            // Delegar al ViewModel (coroutines con reintento)
            viewModel.crearReserva(body)
        }
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida que todos los campos del formulario de reserva estén completos.
     * Muestra un Toast para el primer campo faltante.
     *
     * @return true si todo es válido, false si hay campos vacíos.
     */
    private fun validarFormularioReserva(personas: Int): Boolean {
        if (fechaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show()
            return false
        }
        if (bloqueHoraStr.isEmpty()) {
            Toast.makeText(this, "Selecciona un bloque horario", Toast.LENGTH_SHORT).show()
            return false
        }
        if (tematicaSeleccionadaId == -1) {
            Toast.makeText(this, "Selecciona una temática", Toast.LENGTH_SHORT).show()
            return false
        }
        if (personas <= 0) {
            Toast.makeText(this, "Ingresa el número de personas", Toast.LENGTH_SHORT).show()
            return false
        }
        if (personas > MAX_PERSONAS) {
            Toast.makeText(this, "Máximo $MAX_PERSONAS personas por reserva", Toast.LENGTH_SHORT).show()
            return false
        }
        if (pisoNumInt == -1) {
            Toast.makeText(this, "Selecciona un piso", Toast.LENGTH_SHORT).show()
            return false
        }
        if (metodoPagoStr.isEmpty()) {
            Toast.makeText(this, "Selecciona un método de pago", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // ==================== DIÁLOGO SELECTOR GENÉRICO ====================

    /**
     * Muestra un diálogo personalizado con una lista de opciones y selección visual.
     * Reutilizado por todos los selectores (bloque, piso, pago, temática).
     *
     * Principio DRY: Una sola función para todos los diálogos de selección.
     *
     * @param titulo Título del diálogo.
     * @param opciones Lista de textos a mostrar.
     * @param indiceActual Índice actualmente seleccionado (-1 si ninguno).
     * @param alSeleccionar Lambda que recibe el índice y texto seleccionados.
     */
    private fun mostrarDialogoSelector(
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

            // Estilo visual de selección
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

    companion object {
        /** Número máximo de personas permitido por reserva. */
        private const val MAX_PERSONAS = 10
    }
}

