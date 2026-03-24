package com.sena.myapplication.views

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.sena.myapplication.R
import com.sena.myapplication.databinding.ActivityReservaBinding
import com.sena.myapplication.models.*
import com.sena.myapplication.viewmodels.ViewModelReserva
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale


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

    // Estado para el comprobante de transferencia
    private var comprobanteUri: Uri? = null
    private var dialogTransferenciaActivo: Dialog? = null

    private val selectorComprobante = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            comprobanteUri = it
            dialogTransferenciaActivo?.findViewById<TextView>(R.id.tvNombreArchivo)?.text =
                obtenerNombreArchivo(it)
        }
    }

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

        // --- Listeners de selección (usan mostrarDialogoSelector heredado de BaseActivity) ---

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

            // Si es transferencia, mostrar diálogo con datos bancarios
            if (metodoPagoStr == "transferencia") {
                val total = carritoPrecio * carritoCantidad
                mostrarDialogoTransferencia(total) { comprobanteBase64 ->
                    // Reconstruir body con comprobante incluido
                    val bodyConComprobante = body.copy(
                        reserva = body.reserva.copy(comprobantePago = comprobanteBase64)
                    )
                    viewModel.crearReserva(bodyConComprobante)
                }
            } else {
                // Efectivo: enviar directo
                viewModel.crearReserva(body)
            }
        }
    }

    // ==================== VALIDACIONES ====================


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

    // ==================== DIÁLOGO TRANSFERENCIA ====================


    private fun mostrarDialogoTransferencia(total: Double, alConfirmar: (String) -> Unit) {
        comprobanteUri = null // Resetear comprobante anterior

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_transferencia)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.85f)
        dialogTransferenciaActivo = dialog

        // Formatear el total con separador de miles
        val formato = NumberFormat.getNumberInstance(Locale("es", "CO"))
        formato.maximumFractionDigits = 0
        val totalFormateado = formato.format(total)

        dialog.findViewById<TextView>(R.id.tvTotalPagar).text = "TOTAL A PAGAR: $ $totalFormateado"

        // Zona para seleccionar comprobante
        dialog.findViewById<LinearLayout>(R.id.layoutSeleccionarComprobante).setOnClickListener {
            selectorComprobante.launch("image/*")
        }

        // Botón enviar comprobante → codificar imagen y confirmar
        dialog.findViewById<Button>(R.id.btnEnviarComprobante).setOnClickListener {
            val uri = comprobanteUri
            if (uri == null) {
                Toast.makeText(this, "Selecciona un comprobante de pago", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Codificar imagen a base64 (método heredado de BaseActivity)
            val base64 = codificarImagenBase64(uri)
            if (base64 == null) {
                Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            dialog.dismiss()
            dialogTransferenciaActivo = null
            alConfirmar(base64)
        }

        // Botón cancelar
        dialog.findViewById<Button>(R.id.btnCancelarTransferencia).setOnClickListener {
            dialog.dismiss()
            dialogTransferenciaActivo = null
        }

        dialog.setOnDismissListener { dialogTransferenciaActivo = null }
        dialog.show()
    }


    companion object {
        private const val MAX_PERSONAS = 10
    }
}

