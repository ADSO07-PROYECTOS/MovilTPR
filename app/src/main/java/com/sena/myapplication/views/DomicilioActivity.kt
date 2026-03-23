package com.sena.myapplication.views

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
import com.sena.myapplication.databinding.ActivityDomicilioBinding
import com.sena.myapplication.models.ClienteReserva
import com.sena.myapplication.models.DatosDomicilio
import com.sena.myapplication.models.DomicilioRequest
import com.sena.myapplication.models.PedidoItem
import com.sena.myapplication.viewmodels.ViewModelDomicilio

/**
 * Pantalla de envío a domicilio — Recopila dirección, barrio/referencia
 * y método de pago para completar el pedido.
 *
 * Recibe datos del cliente (CLI_*) y del carrito (CARRITO_*) vía Intent extras.
 * Al confirmar, envía el pedido vía [ViewModelDomicilio] a POST /api/domicilios.
 *
 * Patrón MVVM: La Activity solo gestiona la UI; la lógica de red
 * se delega al ViewModel.
 */
class DomicilioActivity : BaseActivity() {

    private lateinit var binding: ActivityDomicilioBinding
    private lateinit var viewModel: ViewModelDomicilio

    // Estado del selector de método de pago
    private val opcionesMetodoPago = listOf("Efectivo (Contra entrega)", "Transferencia")
    private var metodoPagoPos: Int = 0
    private var metodoPagoStr: String = "efectivo"

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

        // Selector de método de pago con diálogo personalizado
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

            // Delegar al ViewModel (coroutines con reintento)
            viewModel.crearDomicilio(body)
        }
    }

    // ==================== VALIDACIONES ====================

    /**
     * Valida que los campos obligatorios del formulario de envío estén completos.
     *
     * @return true si todo es válido, false si falta algún campo.
     */
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

    // ==================== DIÁLOGO SELECTOR GENÉRICO ====================

    /**
     * Muestra un diálogo personalizado con opciones seleccionables.
     * Reutiliza el mismo layout [R.layout.dialog_selector_generico]
     * que usa [ReservaActivity].
     *
     * Principio DRY: Misma lógica de selector visual.
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

        var indiceTemp = if (indiceActual == -1 && opciones.isNotEmpty()) 0 else indiceActual
        val vistasFilas = mutableListOf<View>()

        for (i in opciones.indices) {
            val vistaFila = LayoutInflater.from(this)
                .inflate(R.layout.item_opcion_dialogo, contenedor, false)
            val tvTexto  = vistaFila.findViewById<TextView>(R.id.tvTextoOpcion)
            val imgCheck = vistaFila.findViewById<ImageButton>(R.id.imgCheckOpcion)

            tvTexto.text = opciones[i]

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
}
