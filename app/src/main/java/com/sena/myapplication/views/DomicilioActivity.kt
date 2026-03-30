package com.sena.myapplication.views

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
import com.sena.myapplication.databinding.ActivityDomicilioBinding
import com.sena.myapplication.models.ClienteReserva
import com.sena.myapplication.models.DatosDomicilio
import com.sena.myapplication.models.DomicilioRequest
import com.sena.myapplication.models.PedidoItem
import com.sena.myapplication.viewmodels.ViewModelDomicilio
import java.text.NumberFormat
import java.util.Locale

class DomicilioActivity : BaseActivity() {

    private lateinit var binding: ActivityDomicilioBinding
    private lateinit var viewModel: ViewModelDomicilio

    // Estado del selector de método de pago
    private val opcionesMetodoPago = listOf("Efectivo (Contra entrega)", "Transferencia")
    private var metodoPagoPos: Int = 0
    private var metodoPagoStr: String = "efectivo"

    // Estado para el comprobante de transferencia
    private var comprobanteUri: Uri? = null
    private var dialogTransferenciaActivo: Dialog? = null

    private val selectorComprobante = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            comprobanteUri = it
            // Usa método heredado de BaseActivity
            dialogTransferenciaActivo?.findViewById<TextView>(R.id.tvNombreArchivo)?.text =
                obtenerNombreArchivo(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDomicilioBinding.inflate(layoutInflater)
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

        // --- Inicializar ViewModel ---
        viewModel = ViewModelProvider(this)[ViewModelDomicilio::class.java]

        // Observar resultado exitoso → navegar a confirmación
        viewModel.pedidoExitoso.observe(this) { response ->
            response?.let {
                val intent = Intent(this, ConfirmacionReservaActivity::class.java)
                intent.putExtra("QR_BASE64", it.qr ?: "")
                startActivity(intent)
                finishAffinity()
            }
        }

        // Observar errores
        viewModel.error.observe(this) { mensajeError ->
            mensajeError?.let {
                Toast.makeText(this, it, Toast.LENGTH_LONG).show()
            }
        }

        // Observar estado de envío para habilitar/deshabilitar botón
        viewModel.enviando.observe(this) { enviando ->
            binding.btnConfirmarPedido.isEnabled = !enviando
            binding.btnConfirmarPedido.text = if (enviando) "Enviando..." else "CONFIRMAR PEDIDO"
        }

        // --- Listeners ---
        binding.btnVolverDomicilio.setOnClickListener { finish() }

        // Selector de metodo de pago (usa mostrarDialogoSelector heredado de BaseActivity)
        binding.btnMetodoPago.setOnClickListener {
            mostrarDialogoSelector("Método de Pago", opcionesMetodoPago, metodoPagoPos) { pos, texto ->
                metodoPagoPos = pos
                metodoPagoStr = when {
                    texto.contains("Efectivo", ignoreCase = true) -> "efectivo"
                    texto.contains("Transferencia", ignoreCase = true) -> "transferencia"
                    else -> texto.lowercase()
                }
                binding.btnMetodoPago.text = texto
            }
        }

        binding.btnConfirmarPedido.setOnClickListener {
            val direccion = binding.etDireccion.text.toString().trim()
            val barrio    = binding.etBarrio.text.toString().trim()

            // Validaciones
            if (!validarFormularioEnvio(direccion, barrio)) return@setOnClickListener

            // Concatenar dirección + barrio en un solo campo (el backend solo almacena "direccion")
            val direccionCompleta = "$direccion - $barrio"

            // Construir el body del request (claves: cliente, domicilio, productos)
            val body = DomicilioRequest(
                cliente = ClienteReserva(
                    doc = cliCedula,
                    nom = cliNombre,
                    correo = cliCorreo,
                    tel = cliTelefono
                ),
                domicilio = DatosDomicilio(
                    direccion = direccionCompleta,
                    metodoPago = metodoPagoStr
                ),
                productos = if (carritoIdPlato != -1) listOf(
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
                        domicilio = body.domicilio.copy(comprobantePago = comprobanteBase64)
                    )
                    viewModel.crearDomicilio(bodyConComprobante)
                }
            } else {
                // Efectivo: enviar directo
                viewModel.crearDomicilio(body)
            }
        }
    }

    // ==================== VALIDACIONES ====================


    private fun validarFormularioEnvio(direccion: String, barrio: String): Boolean {
        if (direccion.isEmpty()) {
            Toast.makeText(this, "Ingresa la dirección completa", Toast.LENGTH_SHORT).show()
            return false
        }
        if (direccion.length < 5) {
            Toast.makeText(this, "La dirección debe tener al menos 5 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        if (barrio.isEmpty()) {
            Toast.makeText(this, "Ingresa el barrio o referencia", Toast.LENGTH_SHORT).show()
            return false
        }
        if (barrio.length < 3) {
            Toast.makeText(this, "El barrio debe tener al menos 3 caracteres", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // ==================== DIÁLOGO TRANSFERENCIA ====================

    private fun mostrarDialogoTransferencia(total: Double, alConfirmar: (String) -> Unit) {
        comprobanteUri = null

        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_transferencia)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setDimAmount(0.85f)
        dialogTransferenciaActivo = dialog

        val formato = NumberFormat.getNumberInstance(Locale("es", "CO"))
        formato.maximumFractionDigits = 0
        val totalFormateado = formato.format(total)

        dialog.findViewById<TextView>(R.id.tvTotalPagar).text = "TOTAL A PAGAR: $ $totalFormateado"

        dialog.findViewById<LinearLayout>(R.id.layoutSeleccionarComprobante).setOnClickListener {
            selectorComprobante.launch("image/*")
        }

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

        dialog.findViewById<Button>(R.id.btnCancelarTransferencia).setOnClickListener {
            dialog.dismiss()
            dialogTransferenciaActivo = null
        }

        dialog.setOnDismissListener { dialogTransferenciaActivo = null }
        dialog.show()
    }
}
