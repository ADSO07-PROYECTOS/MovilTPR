package com.sena.myapplication

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.sena.myapplication.conexion.ReservaRetrofitClient
import com.sena.myapplication.models.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class ReservaActivity : BaseActivity() {

  private val api = ReservaRetrofitClient.instance
  private var listaTematicas: List<TematicaModel> = emptyList()

  // Variables de Estado de la Reserva
  private var fechaSeleccionada = ""
  private var bloqueHoraStr: String = ""
  private var bloquePos: Int = -1

  private var tematicaSeleccionadaId: Int = -1
  private var tematicaPos: Int = -1

  private var pisoNumInt: Int = -1
  private var pisoPos: Int = -1

  private var metodoPagoStr: String = ""
  private var metodoPos: Int = -1

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_reserva)
    configurarBarraNavegacion()

    val btnFecha = findViewById<Button>(R.id.btnFecha)
    val btnBloque = findViewById<Button>(R.id.btnBloque)
    val btnTematica = findViewById<Button>(R.id.btnTematica)
    val btnPiso = findViewById<Button>(R.id.btnPiso)
    val btnMetodoPago = findViewById<Button>(R.id.btnMetodoPago)
    val etPersonas = findViewById<TextInputEditText>(R.id.etPersonas)
    val btnVolver = findViewById<Button>(R.id.btnVolverReserva)
    val btnReservar = findViewById<Button>(R.id.btnReservar)

    val cliNombre = intent.getStringExtra("CLI_NOMBRE") ?: ""
    val cliCedula = intent.getStringExtra("CLI_CEDULA") ?: ""
    val cliCorreo = intent.getStringExtra("CLI_CORREO") ?: ""
    val cliTelefono = intent.getStringExtra("CLI_TELEFONO") ?: ""

    val carritoIdPlato = intent.getIntExtra("CARRITO_ID_PLATO", -1)
    val carritoCantidad = intent.getIntExtra("CARRITO_CANTIDAD", 1)
    val carritoPrecio = intent.getDoubleExtra("CARRITO_PRECIO_UNITARIO", 0.0)

    val opcionesBloque = (6..22).map { "$it:00" }
    val opcionesPiso = listOf("Piso 1", "Piso 2")
    val opcionesMetodo = listOf("Efectivo", "Transferencia")


    btnFecha.setOnClickListener {
      val mañana = Calendar.getInstance()
      mañana.add(Calendar.DAY_OF_YEAR, 1)

      val picker = DatePickerDialog(
        this, R.style.CalendarioPersonalizado, { _, y, m, d ->
          val mes = String.format("%02d", m + 1)
          val dia = String.format("%02d", d)
          fechaSeleccionada = "$y-$mes-$dia"
          btnFecha.text = fechaSeleccionada
        }, mañana.get(Calendar.YEAR), mañana.get(Calendar.MONTH), mañana.get(Calendar.DAY_OF_MONTH)
      )
      picker.datePicker.minDate = mañana.timeInMillis
      picker.show()
    }

    btnBloque.setOnClickListener {
      mostrarDialogoSelector("Bloque Horario", opcionesBloque, bloquePos) { pos, texto ->
        bloquePos = pos
        bloqueHoraStr = texto.replace(":00", "")
        btnBloque.text = texto
      }
    }

    btnPiso.setOnClickListener {
      mostrarDialogoSelector("Seleccionar Piso", opcionesPiso, pisoPos) { pos, texto ->
        pisoPos = pos
        pisoNumInt = pos + 1
        btnPiso.text = texto
      }
    }

    btnMetodoPago.setOnClickListener {
      mostrarDialogoSelector("Método de Pago", opcionesMetodo, metodoPos) { pos, texto ->
        metodoPos = pos
        metodoPagoStr = texto.lowercase()
        btnMetodoPago.text = texto
      }
    }

    btnTematica.setOnClickListener {
      val nombresTematicas = listaTematicas.map { it.nombre }
      mostrarDialogoSelector("Temática", nombresTematicas, tematicaPos) { pos, texto ->
        tematicaPos = pos
        tematicaSeleccionadaId = listaTematicas[pos].id
        btnTematica.text = texto
      }
    }

    // Cargar las temáticas al iniciar
    cargarTematicas(btnTematica)

    // 3. ENVÍO AL SERVIDOR
    btnVolver.setOnClickListener { finish() }

    btnReservar.setOnClickListener {
      val personas = etPersonas.text.toString().trim().toIntOrNull() ?: 0

      // Validaciones Estrictas
      if (fechaSeleccionada.isEmpty()) {
        Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT)
          .show(); return@setOnClickListener
      }
      if (bloqueHoraStr.isEmpty()) {
        Toast.makeText(this, "Selecciona un bloque horario", Toast.LENGTH_SHORT)
          .show(); return@setOnClickListener
      }
      if (tematicaSeleccionadaId == -1) {
        Toast.makeText(this, "Selecciona una temática", Toast.LENGTH_SHORT)
          .show(); return@setOnClickListener
      }
      if (personas <= 0) {
        Toast.makeText(this, "Ingresa el número de personas", Toast.LENGTH_SHORT)
          .show(); return@setOnClickListener
      }
      if (pisoNumInt == -1) {
        Toast.makeText(this, "Selecciona un piso", Toast.LENGTH_SHORT)
          .show(); return@setOnClickListener
      }
      if (metodoPagoStr.isEmpty()) {
        Toast.makeText(this, "Selecciona un método de pago", Toast.LENGTH_SHORT)
          .show(); return@setOnClickListener
      }

      btnReservar.isEnabled = false
      btnReservar.text = "Enviando..."

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
        ) else listOf()
      )

      Log.d(
        "ReservaActivity",
        "Enviando reserva: cliente=${body.cliente}, reserva=${body.reserva}, pedido=${body.pedido}"
      )

      var intentosReserva = 0
      fun enviarReserva() {
        api.crearReserva(body).enqueue(object : Callback<ReservaResponse> {
          override fun onResponse(
            call: Call<ReservaResponse>,
            response: Response<ReservaResponse>
          ) {
            btnReservar.isEnabled = true
            btnReservar.text = "RESERVAR"
            Log.d("ReservaActivity", "Response code: ${response.code()}")
            Log.d("ReservaActivity", "Response body: ${response.body()}")

            if (response.isSuccessful && response.body()?.status == "success") {
              val intent = Intent(this@ReservaActivity, ConfirmacionReservaActivity::class.java)
              intent.putExtra("QR_BASE64", response.body()?.qr ?: "")
              startActivity(intent)
              finishAffinity()
            } else {
              val errorBodyStr = try {
                response.errorBody()?.string()
              } catch (_: Exception) {
                null
              }
              Log.e("ReservaActivity", "Error body: $errorBodyStr")

              val mensajeError = response.body()?.message
                ?: errorBodyStr
                ?: "Error al crear la reserva (código ${response.code()})"
              Toast.makeText(this@ReservaActivity, mensajeError, Toast.LENGTH_LONG).show()
            }
          }

          override fun onFailure(call: Call<ReservaResponse>, t: Throwable) {
            Log.e("ReservaActivity", "onFailure: ${t.message}", t)
            if (intentosReserva < 2) {
              intentosReserva++
              enviarReserva()
            } else {
              btnReservar.isEnabled = true
              btnReservar.text = "RESERVAR"
              Toast.makeText(this@ReservaActivity, "Error de red: ${t.message}", Toast.LENGTH_LONG)
                .show()
            }
          }
        })
      }
      enviarReserva()
    }
  }

  // --- FUNCIONES AUXILIARES ---

  private fun cargarTematicas(btnTematica: Button, intentos: Int = 0) {
    api.obtenerTematicas().enqueue(object : Callback<List<TematicaModel>> {
      override fun onResponse(
        call: Call<List<TematicaModel>>,
        response: Response<List<TematicaModel>>
      ) {
        if (response.isSuccessful && response.body() != null) {
          listaTematicas = response.body()!!
          btnTematica.text = "Seleccionar Temática"
          btnTematica.isEnabled = true // Habilitamos el botón ahora que hay datos
        } else if (intentos < 3) cargarTematicas(btnTematica, intentos + 1)
      }

      override fun onFailure(call: Call<List<TematicaModel>>, t: Throwable) {
        if (intentos < 3) cargarTematicas(btnTematica, intentos + 1)
        else Toast.makeText(this@ReservaActivity, "Error al cargar temáticas", Toast.LENGTH_SHORT)
          .show()
      }
    })
  }

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

    // Si indiceActual es -1 (ninguno seleccionado), forzamos a que el 0 (el primero) sea el seleccionado.
    var indiceTemp = if (indiceActual == -1 && opciones.isNotEmpty()) 0 else indiceActual

    val vistasFilas = mutableListOf<View>()

    for (i in opciones.indices) {
      val vistaFila =
        LayoutInflater.from(this).inflate(R.layout.item_opcion_dialogo, contenedor, false)
      val tvTexto = vistaFila.findViewById<TextView>(R.id.tvTextoOpcion)
      val imgCheck = vistaFila.findViewById<ImageButton>(R.id.imgCheckOpcion)

      tvTexto.text = opciones[i]

      if (i == indiceTemp) {
        imgCheck.setImageResource(R.drawable.ic_reserva)
        tvTexto.setTextColor(Color.parseColor("#A0CBFC"))
      } else {
        imgCheck.setImageResource(R.drawable.ic_mas)
        tvTexto.setTextColor(Color.parseColor("#CCF5F5F5"))
      }

      vistaFila.setOnClickListener {
        indiceTemp = i
        vistasFilas.forEachIndexed { index, vista ->
          val img = vista.findViewById<ImageButton>(R.id.imgCheckOpcion)
          val txt = vista.findViewById<TextView>(R.id.tvTextoOpcion)

          if (index == indiceTemp) {
            img.setImageResource(R.drawable.ic_reserva)
            txt.setTextColor(Color.parseColor("#A0CBFC"))
          } else {
            img.setImageResource(R.drawable.ic_mas)
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
}
