package com.sena.myapplication

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.sena.myapplication.conexion.ReservaRetrofitClient
import com.sena.myapplication.models.ClienteReserva
import com.sena.myapplication.models.DatosReserva
import com.sena.myapplication.models.ReservaRequest
import com.sena.myapplication.models.ReservaResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class ReservaActivity : BaseActivity() {

    private val api = ReservaRetrofitClient.instance
    private var listaTematicas: List<Tematica> = emptyList()
    private var tematicaSeleccionadaId: Int = -1
    private var fechaSeleccionada: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reserva)
        configurarBarraNavegacion()

        // Vistas
        val etFecha          = findViewById<TextInputEditText>(R.id.etFecha)
        val spinnerBloque    = findViewById<Spinner>(R.id.spinnerBloque)
        val spinnerTematica  = findViewById<Spinner>(R.id.spinnerTematica)
        val etPersonas       = findViewById<TextInputEditText>(R.id.etPersonas)
        val spinnerPiso      = findViewById<Spinner>(R.id.spinnerPiso)
        val spinnerMetodo    = findViewById<Spinner>(R.id.spinnerMetodoPago)
        val btnVolver        = findViewById<Button>(R.id.btnVolverReserva)
        val btnReservar      = findViewById<Button>(R.id.btnReservar)

        // Datos cliente y carrito desde el intent
        val cliNombre   = intent.getStringExtra("CLI_NOMBRE")   ?: ""
        val cliCedula   = intent.getStringExtra("CLI_CEDULA")   ?: ""
        val cliCorreo   = intent.getStringExtra("CLI_CORREO")   ?: ""
        val cliTelefono = intent.getStringExtra("CLI_TELEFONO") ?: ""
        val carritoNombre   = intent.getStringExtra("CARRITO_NOMBRE") ?: ""
        val carritoCantidad = intent.getIntExtra("CARRITO_CANTIDAD", 1)
        val carritoPrecio   = intent.getDoubleExtra("CARRITO_PRECIO_UNITARIO", 0.0)

        // Spinner Bloques Horarios (6 a 22)
        val bloques = (6..22).map { "$it:00" }
        ArrayAdapter(this, R.layout.item_spinner_tamano, bloques)
            .also { it.setDropDownViewResource(R.layout.item_spinner_tamano); spinnerBloque.adapter = it }

        // Spinner Pisos
        val pisos = listOf("Piso 1", "Piso 2", "Piso 3")
        ArrayAdapter(this, R.layout.item_spinner_tamano, pisos)
            .also { it.setDropDownViewResource(R.layout.item_spinner_tamano); spinnerPiso.adapter = it }

        // Spinner Métodos de pago
        val metodos = listOf("Efectivo", "Transferencia")
        ArrayAdapter(this, R.layout.item_spinner_tamano, metodos)
            .also { it.setDropDownViewResource(R.layout.item_spinner_tamano); spinnerMetodo.adapter = it }

        // DatePicker al tocar el campo fecha
        etFecha.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                fechaSeleccionada = "%04d-%02d-%02d".format(year, month + 1, day)
                etFecha.setText("%02d/%02d/%04d".format(day, month + 1, year))
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // Cargar temáticas del servidor
        cargarTematicas(spinnerTematica)

        btnVolver.setOnClickListener { finish() }

        btnReservar.setOnClickListener {
            val personas = etPersonas.text.toString().trim().toIntOrNull() ?: 0

            if (fechaSeleccionada.isEmpty()) {
                Toast.makeText(this, "Selecciona una fecha", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            if (tematicaSeleccionadaId == -1) {
                Toast.makeText(this, "Selecciona una temática", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }
            if (personas <= 0) {
                Toast.makeText(this, "Ingresa el número de personas", Toast.LENGTH_SHORT).show(); return@setOnClickListener
            }

            val bloqueHora = spinnerBloque.selectedItem.toString().replace(":00","").toIntOrNull() ?: 6
            val pisoNum    = spinnerPiso.selectedItemPosition + 1
            val metodoPago = if (spinnerMetodo.selectedItemPosition == 1) "transferencia" else "efectivo"

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
                hor = bloqueHora,
                tematica = tematicaSeleccionadaId,
                personas = personas,
                piso = pisoNum,
                metodoPago = metodoPago
              ),
              pedido = listOf() // pedido vacío por ahora
            )

            var intentosReserva = 0

            fun enviarReserva() {
                api.crearReserva(body).enqueue(object : Callback<ReservaResponse> {
                    override fun onResponse(call: Call<ReservaResponse>, response: Response<ReservaResponse>) {
                        btnReservar.isEnabled = true
                        btnReservar.text = "RESERVAR"
                        if (response.isSuccessful && response.body()?.status == "success") {
                            val qrBase64 = response.body()?.qr ?: ""
                            val intent = Intent(this@ReservaActivity, ConfirmacionReservaActivity::class.java)
                            intent.putExtra("QR_BASE64", qrBase64)
                            startActivity(intent)
                            finishAffinity()
                        } else {
                            val msg = response.body()?.message ?: "Error al crear la reserva"
                            Toast.makeText(this@ReservaActivity, msg, Toast.LENGTH_LONG).show()
                            Log.e("ReservaActivity", "Error: ${response.code()} - $msg")
                        }
                    }
                    override fun onFailure(call: Call<ReservaResponse>, t: Throwable) {
                        Log.e("ReservaActivity", "Fallo reserva (intento $intentosReserva): ${t.message}")
                        if (intentosReserva < 2) {
                            intentosReserva++
                            enviarReserva()
                        } else {
                            btnReservar.isEnabled = true
                            btnReservar.text = "RESERVAR"
                            Toast.makeText(this@ReservaActivity,
                                "Error de conexión: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                })
            }
            enviarReserva()
        }
    }

    private fun cargarTematicas(spinner: Spinner, intentos: Int = 0) {
        api.obtenerTematicas().enqueue(object : Callback<List<Tematica>> {
            override fun onResponse(call: Call<List<Tematica>>, response: Response<List<Tematica>>) {
                if (response.isSuccessful && response.body() != null) {
                    listaTematicas = response.body()!!
                    val nombres = listOf("Selecciona una temática...") + listaTematicas.map { it.nombre }
                    val adapter = ArrayAdapter(this@ReservaActivity, R.layout.item_spinner_tamano, nombres)
                    adapter.setDropDownViewResource(R.layout.item_spinner_tamano)
                    spinner.adapter = adapter
                    spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(p: AdapterView<*>, v: View?, pos: Int, id: Long) {
                            tematicaSeleccionadaId = if (pos == 0) -1 else listaTematicas[pos - 1].id
                        }
                        override fun onNothingSelected(p: AdapterView<*>) {}
                    }
                } else if (intentos < 3) {
                    cargarTematicas(spinner, intentos + 1)
                } else {
                    Toast.makeText(this@ReservaActivity,
                        "No se pudieron cargar las temáticas", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<Tematica>>, t: Throwable) {
                Log.e("ReservaActivity", "Fallo temáticas (intento $intentos): ${t.message}")
                if (intentos < 3) {
                    cargarTematicas(spinner, intentos + 1)
                } else {
                    Toast.makeText(this@ReservaActivity,
                        "Error de conexión: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}

