# 📋 DOCUMENTO DE SUSTENTACIÓN — ReservasMovil (MovilTPR)
## Aplicación Android para Restaurante Tres Pasos / Sabores Unidos
### Autor: David | SENA | Instructor: Diego Pinilla

---

## 📌 1. DESCRIPCIÓN GENERAL DEL PROYECTO

Esta es una aplicación Android nativa desarrollada en **Kotlin** que permite a los usuarios del restaurante:

1. **Explorar el menú** por categorías (Entradas, Platos fuertes, Bebidas, etc.)
2. **Ver los platos** de cada categoría con su precio, descripción e imagen
3. **Personalizar su pedido** eligiendo tamaño, sabores y adiciones
4. **Agregar al carrito** con control de cantidad
5. **Elegir entre reservar en restaurante o pedir a domicilio**
6. **Ingresar datos personales** (nombre, cédula, correo, teléfono)
7. **Completar la reserva** (fecha, hora, temática, piso, método de pago)
8. **Completar el domicilio** (dirección, barrio, método de pago)
9. **Pago por transferencia** con pantalla de datos bancarios y subida de comprobante
10. **Recibir confirmación con código QR** que puede descargar a la galería

El backend está compuesto por **3 microservicios Flask (Python)** en un servidor remoto:
- **Puerto 5001** → API de menú (categorías, platos, extras)
- **Puerto 5005** → API de reservas (temáticas, crear reserva)
- **Puerto 5004** → API de domicilios (crear pedido a domicilio)
- **Puerto 5000** → Servidor de imágenes estáticas

**Logo de la marca:** Se usa `saboresunidos.png` (en `res/drawable/`) como imagen de identidad visual en todas las pantallas principales: MainActivity, DatosClienteActivity, ReservaActivity, DomicilioActivity, ConfirmacionReservaActivity y el diálogo de transferencia.

---

## 📌 2. ARQUITECTURA: PATRÓN MVVM

La app usa el patrón **Model-View-ViewModel** que separa la lógica en 3 capas:

```
┌─────────────────────────────────────────────────────────────┐
│  VIEW (Activities)                                          │
│  - Solo maneja la interfaz de usuario (UI)                  │
│  - Observa los LiveData del ViewModel                       │
│  - NO hace llamadas de red directamente                     │
├─────────────────────────────────────────────────────────────┤
│  VIEWMODEL                                                  │
│  - Contiene la lógica de negocio                            │
│  - Llama a los servicios Retrofit con coroutines            │
│  - Expone los datos vía LiveData                            │
│  - Implementa reintentos automáticos (máx. 2)               │
├─────────────────────────────────────────────────────────────┤
│  MODEL + SERVICE                                            │
│  - Data classes que representan los datos del API           │
│  - Interfaces Retrofit que definen los endpoints            │
│  - Configuración de OkHttp + Gson                           │
└─────────────────────────────────────────────────────────────┘
```

**¿Por qué MVVM?**
- La Activity (View) no se mezcla con lógica de red → **código más limpio**
- Si el usuario rota el celular, el ViewModel **sobrevive** y no pierde los datos
- Los LiveData actualizan la UI **automáticamente** cuando cambian los datos

---

## 📌 3. ESTRUCTURA DE PAQUETES

```
com.sena.myapplication/
│
├── models/          → Data classes con @SerializedName
│   ├── ModelCategoria.kt
│   ├── ModelPlato.kt
│   ├── ModelExtras.kt (contiene ModelTamano, ModelSabor, ModelAdicion)
│   ├── ModelTamano.kt
│   ├── ModelSabor.kt
│   ├── ModelAdicion.kt
│   ├── ModelTematica.kt
│   ├── ModelReserva.kt (ReservaRequest, ReservaResponse, ClienteReserva, etc.)
│   ├── ModelDomicilio.kt (DomicilioRequest, DatosDomicilio)
│   └── ModelCarritoItem.kt
│
├── services/        → Interfaces Retrofit con companion object
│   ├── ConexionServiceMenu.kt       → Puerto 5001 (categorías, platos, extras)
│   ├── ConexionServiceReserva.kt    → Puerto 5005 (temáticas, reservas)
│   └── ConexionServiceDomicilio.kt  → Puerto 5004 (domicilios)
│
├── viewmodels/      → ViewModels con coroutines y LiveData
│   ├── ViewModelCategoria.kt
│   ├── ViewModelPlato.kt
│   ├── ViewModelPlatoDetalle.kt
│   ├── ViewModelReserva.kt
│   └── ViewModelDomicilio.kt
│
├── adapters/        → RecyclerView Adapters
│   ├── AdapterCategoria.kt
│   └── AdapterPlato.kt
│
└── views/           → Activities (todas extienden BaseActivity)
    ├── BaseActivity.kt
    ├── MainActivity.kt
    ├── VerPlatosActivity.kt
    ├── PlatoDetalleActivity.kt
    ├── CarritoActivity.kt
    ├── DatosClienteActivity.kt
    ├── ReservaActivity.kt
    ├── DomicilioActivity.kt
    └── ConfirmacionReservaActivity.kt
```

---

## 📌 4. FLUJO COMPLETO DE LA APLICACIÓN (Activity Chain)

```
MainActivity (grilla de categorías)
  │
  └→ VerPlatosActivity (lista de platos de la categoría)
       │
       └→ PlatoDetalleActivity (tamaño, sabores, adiciones, cantidad)
            │
            └→ CarritoActivity (resumen, ajustar cantidad)
                 │
                 ├→ [Restaurante] → DatosClienteActivity → ReservaActivity → ConfirmacionReservaActivity (QR)
                 │
                 └→ [Domicilio]   → DatosClienteActivity → DomicilioActivity → ConfirmacionReservaActivity (QR)
```

**La comunicación entre Activities se hace por `Intent extras`** con prefijos:
- `CARRITO_*` → datos del plato/carrito
- `CLI_*` → datos del cliente
- `FLUJO_DOMICILIO` → flag booleano que decide si va a reserva o domicilio
- `QR_BASE64` → imagen QR codificada para la pantalla de confirmación

---

## 📌 5. EXPLICACIÓN DETALLADA DE CADA ARCHIVO

---

### 🔷 5.1 VIEWS (Pantallas)

---

#### `BaseActivity.kt` — Clase base de todas las Activities

**¿Qué hace?** Es la clase padre de la que heredan TODAS las activities. Configura la barra de navegación inferior (botones Casa, Carrito, Notificación).

**Función clave:**
```kotlin
protected fun configurarBarraNavegacion()
```
- Busca los 3 botones de la barra inferior (`btnCasa`, `btnCarrito`, `btnNotification`)
- Si alguno no existe en el layout actual, retorna silenciosamente (usa `?: return`)
- `btnCasa` → Navega a `MainActivity` limpiando la pila con `FLAG_ACTIVITY_CLEAR_TOP`
- `btnCarrito` → Abre `CarritoActivity`
- `btnNotificacion` → Pendiente (TODO)

**¿Por qué existe?** Principio DRY — en vez de copiar el código de la barra de navegación en las 9 activities, lo centralizamos aquí y cada activity simplemente llama `configurarBarraNavegacion()` después de `setContentView`.

---

#### `MainActivity.kt` — Pantalla principal (Categorías)

**¿Qué hace?** Es la primera pantalla que ve el usuario (launcher). Muestra las categorías del menú en una grilla de 2 columnas.

**¿Cómo funciona paso a paso?**

1. `binding = ActivityMain3Binding.inflate(layoutInflater)` → Infla el layout usando **ViewBinding** (no usamos `findViewById` directamente)
2. `configurarBarraNavegacion()` → Activa la barra de navegación inferior heredada de BaseActivity
3. `binding.rvCategorias.layoutManager = GridLayoutManager(this, 2)` → Configura el RecyclerView como grilla de 2 columnas
4. `adapter = AdapterCategoria(emptyList()) { abrirPlatos(it) }` → Crea el adapter con lista vacía y un lambda que se ejecutará al hacer clic en una categoría
5. `viewModel = ViewModelProvider(this)[ViewModelCategoria::class.java]` → Obtiene el ViewModel (Android lo crea o reutiliza automáticamente)
6. `viewModel.categorias.observe(this) { categorias -> ... }` → Se "suscribe" al LiveData; cada vez que el ViewModel actualice la lista, la UI se actualiza automáticamente
7. `viewModel.obtenerCategorias()` → Dispara la llamada al API

**Función `abrirPlatos(categoria)`:**
```kotlin
private fun abrirPlatos(categoria: ModelCategoria) {
    val intent = Intent(this, VerPlatosActivity::class.java).apply {
        putExtra("ID_CATEGORIA", categoria.id)
        putExtra("NOMBRE_CATEGORIA", categoria.nombre)
    }
    startActivity(intent)
}
```
Crea un Intent con los datos de la categoría y abre la siguiente pantalla.

---

#### `VerPlatosActivity.kt` — Lista de platos por categoría

**¿Qué hace?** Recibe el ID de la categoría seleccionada y muestra los platos de esa categoría en una lista vertical.

**Flujo:**
1. Recibe `ID_CATEGORIA` y `NOMBRE_CATEGORIA` del Intent
2. Muestra el nombre de la categoría como título
3. Usa `ViewModelPlato` para cargar platos de esa categoría (`/api/platos/{id}`)
4. Al tocar un plato, llama a `abrirDetalle(plato)` que navega a `PlatoDetalleActivity` pasando ID, nombre, descripción, imagen y precio del plato

---

#### `PlatoDetalleActivity.kt` — Detalle y personalización del plato

**¿Qué hace?** Permite al usuario personalizar su plato: elegir tamaño, sabores, adiciones y cantidad.

**Variables de estado:**
```kotlin
private var listaTamanos: List<ModelTamano> = emptyList()    // Tamaños disponibles
private var tamanoSeleccionado: ModelTamano? = null           // Tamaño elegido
private var precioTamanoSeleccionado: Double = 0.0            // Precio del tamaño
private var precioAdicionesTotal: Double = 0.0                // Suma de adiciones
private var cantidad: Int = 1                                  // Cantidad seleccionada
```

**Funciones clave:**

- **`observarTamanos()`** → Se suscribe al LiveData de tamaños. Cuando llegan del servidor, selecciona el primero por defecto.
- **`seleccionarTamano(tamano)`** → Actualiza la UI con el tamaño elegido, cambia el precio y muestra/oculta los sabores si el tamaño permite más de 1 sabor.
- **`mostrarDialogoTamanos()`** → Abre un AlertDialog con las opciones de tamaño y su precio.
- **`observarSabores()`** → Genera dinámicamente views con checkboxes para cada sabor usando `LayoutInflater`.
- **`observarAdiciones()`** → Genera dinámicamente views con botones +/- para cada adición. Cada adición suma/resta al `precioAdicionesTotal`.
- **`actualizarPrecioTotal()`** → Calcula `(tamaño + adiciones) × cantidad` y lo muestra formateado.

**Carga de imágenes con Glide:**
```kotlin
val imagenCompleta = if (imagenUrl.startsWith("http")) imagenUrl
                     else "${ConexionServiceMenu.BASE_URL_IMAGENES}$imagenUrl"
Glide.with(this).load(imagenCompleta).placeholder(R.drawable.plato1).into(binding.imgPlato)
```
Si la URL ya es completa la usa directamente; si solo viene el nombre del archivo, le antepone la URL base de imágenes (`http://147.182.238.195:5000/static/img/`).

**Al presionar "Añadir":**
Navega a `CarritoActivity` pasando: `CARRITO_ID_PLATO`, `CARRITO_NOMBRE`, `CARRITO_TAMANO`, `CARRITO_CANTIDAD`, `CARRITO_PRECIO_UNITARIO`, `CARRITO_PRECIO_TOTAL`.

---

#### `CarritoActivity.kt` — Carrito de compras

**¿Qué hace?** Muestra el resumen del plato seleccionado y permite ajustar la cantidad antes de proceder.

**Detección de carrito vacío:**
```kotlin
if (idPlato == -1) {
    binding.cardPlato.visibility = View.GONE
    binding.tvCarritoVacio.visibility = View.VISIBLE
    return
}
```
Si no se recibió un plato válido (el usuario entró al carrito desde la barra de navegación sin haber agregado nada), oculta la tarjeta del plato y muestra el mensaje "Carrito vacío".

**Función `actualizarUI()`:** Recalcula el precio total `precioUnitario × cantidad` y actualiza los TextViews del precio y el botón "Ir a pagar".

**Función `mostrarDialogoPedido()`:**
- Infla el layout `dialog_tipo_pedido.xml` con 2 opciones
- **"Reservar en restaurante"** → Navega a `DatosClienteActivity` SIN flag de domicilio
- **"Pedir a domicilio"** → Navega a `DatosClienteActivity` CON `putExtra("FLUJO_DOMICILIO", true)`
- **"Cancelar"** → Cierra el diálogo

---

#### `DatosClienteActivity.kt` — Formulario del cliente

**¿Qué hace?** Recopila nombre, cédula, correo y teléfono del usuario antes de crear la reserva o el domicilio.

**Validaciones implementadas:**
| Campo | Validación |
|-------|-----------|
| Nombre | No vacío, mínimo 3 caracteres |
| Cédula | No vacía, mínimo 6 dígitos |
| Correo | No vacío + formato válido con `Patterns.EMAIL_ADDRESS` |
| Teléfono | No vacío, exactamente 10 dígitos |
| Habeas Data | Checkbox obligatorio marcado |

**Decisión de flujo:**
```kotlin
val destino = if (esDomicilio) DomicilioActivity::class.java
              else ReservaActivity::class.java
```
Si el flag `FLUJO_DOMICILIO` es `true`, navega a `DomicilioActivity`; si es `false`, va a `ReservaActivity`. Propaga tanto los datos del cliente (`CLI_*`) como los del carrito (`CARRITO_*`).

---

#### `ReservaActivity.kt` — Formulario de reserva

**¿Qué hace?** El usuario completa: fecha, bloque horario, temática, número de personas, piso y método de pago.

**Variables de estado (selecciones del formulario):**
```kotlin
private var fechaSeleccionada = ""           // "2026-03-25"
private var bloqueHoraStr = ""               // "18" (hora sin :00)
private var tematicaSeleccionadaId = -1      // ID de la temática
private var pisoNumInt = -1                   // 1 o 2
private var metodoPagoStr = ""               // "efectivo" o "transferencia"
```

**¿Cómo se cargan las temáticas?**
1. `viewModel.cargarTematicas()` → El ViewModel llama a `GET /api/tematicas` en el puerto 5005
2. `viewModel.tematicas.observe(this)` → Cuando llegan, se guardan en `listaTematicas` y se habilita el botón

**Observers del ViewModel:**
- `viewModel.reservaExitosa.observe` → Cuando el servidor responde éxito, extrae el QR en base64 y navega a `ConfirmacionReservaActivity`
- `viewModel.error.observe` → Muestra un Toast con el error
- `viewModel.enviando.observe` → Deshabilita el botón y muestra "Enviando..." mientras se procesa

**Diálogo selector genérico (`mostrarDialogoSelector`):**
Esta función se reutiliza para TODOS los selectores (bloque horario, piso, método de pago, temática). Recibe:
- `titulo`: El título del diálogo
- `opciones`: Lista de textos a mostrar
- `indiceActual`: Cuál está seleccionado actualmente
- `alSeleccionar`: Lambda que se ejecuta cuando el usuario elige una opción

Crea dinámicamente filas con un ícono de radio button y texto, aplicando estilo visual blanco al seleccionado y semitransparente a los demás.

**Validaciones del formulario:**
```kotlin
private fun validarFormularioReserva(personas: Int): Boolean
```
Valida que TODOS los campos estén seleccionados. Si alguno falta, muestra un Toast y retorna `false`. Valida que personas sea entre 1 y 10 (`MAX_PERSONAS`).

**Flujo de pago por transferencia:**
Si `metodoPagoStr == "transferencia"`, antes de enviar la reserva se muestra el diálogo `mostrarDialogoTransferencia()` con:
- Datos bancarios (Bancolombia, Ahorros, NIT)
- Advertencia de no devoluciones
- Total a pagar formateado con separador de miles
- Zona para seleccionar comprobante de pago (imagen desde galería)
- Si el usuario no selecciona comprobante y toca "Enviar", muestra Toast de error
- Una vez seleccionado el comprobante, al tocar "Enviar Comprobante" se ejecuta `viewModel.crearReserva(body)`

**Construcción del body JSON:**
```kotlin
val body = ReservaRequest(
    cliente = ClienteReserva(doc, nom, correo, tel),
    reserva = DatosReserva(fec, hor, tematica, personas, piso, metodoPago),
    pedido  = listOf(PedidoItem(id, cantidad, precio))
)
```

---

#### `DomicilioActivity.kt` — Formulario de domicilio

**¿Qué hace?** Similar a ReservaActivity pero para pedidos a domicilio. Campos: dirección, barrio/referencia, método de pago.

**Validaciones:**
- Dirección no vacía, mínimo 5 caracteres
- Barrio no vacío, mínimo 3 caracteres
- `maxLength="80"` en dirección y `maxLength="60"` en barrio (en el XML)

**Pago por transferencia:** Exactamente igual que en ReservaActivity — muestra el diálogo con datos bancarios y zona de comprobante.

**Body JSON que envía al backend:**
```kotlin
val body = DomicilioRequest(
    cliente = ClienteReserva(doc, nom, correo, tel),
    domicilio = DatosDomicilio(direccion = "$direccion - $barrio", metodoPago),
    productos = listOf(PedidoItem(id, cantidad, precio))
)
```
Nota: dirección y barrio se concatenan porque el backend solo tiene un campo `direccion`.

---

#### `ConfirmacionReservaActivity.kt` — Pantalla de confirmación con QR

**¿Qué hace?** Muestra el código QR que el servidor generó y permite descargarlo.

**Decodificación del QR:**
```kotlin
val bytes = Base64.decode(qrBase64, Base64.DEFAULT)       // String base64 → bytes
qrBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) // bytes → Bitmap
binding.imgQr.setImageBitmap(qrBitmap)                    // Bitmap → ImageView
```

**Función `guardarQrEnGaleria()`:**
- **Android 10+ (API 29+):** Usa `MediaStore` — NO necesita permisos. Crea un registro en la tabla de imágenes del sistema con `ContentValues`, escribe el bitmap como PNG, y lo marca como disponible.
- **Android 9 y anterior:** Escribe directamente un archivo en `Pictures/SaboresUnidos/` y notifica a la galería con `ACTION_MEDIA_SCANNER_SCAN_FILE`.
- El nombre del archivo incluye timestamp para evitar duplicados: `QR_Pedido_1711234567890.png`

---

### 🔷 5.2 VIEWMODELS (Lógica de negocio)

---

#### `ViewModelCategoria.kt` — ViewModel de categorías

**¿Qué hace?** Obtiene la lista de categorías desde `GET /api/categorias` (puerto 5001).

**Patrón de LiveData encapsulado:**
```kotlin
private val _categorias = MutableLiveData<List<ModelCategoria>>()  // Privado y mutable
val categorias: LiveData<List<ModelCategoria>> get() = _categorias // Público e inmutable
```
¿Por qué? La Activity solo puede LEER los datos (`LiveData`), no puede ESCRIBIR en ellos. Solo el ViewModel tiene permiso de modificar el `MutableLiveData`. Esto protege los datos.

**Mecanismo de reintento automático:**
```kotlin
private fun realizarLlamada(intentos: Int) {
    viewModelScope.launch {
        try {
            val response = api.obtenerCategorias()
            // Si es exitoso → actualizar LiveData
        } catch (e: Exception) {
            if (intentos < MAX_REINTENTOS) {       // MAX_REINTENTOS = 2
                realizarLlamada(intentos + 1)       // Reintentar
                return@launch
            }
            _error.value = "Error de conexión"      // Falló 3 veces → reportar error
        }
    }
}
```
Usa **recursión**: si falla, se llama a sí misma con `intentos + 1`. Cuando llega a 2, deja de reintentar y muestra error.

**`viewModelScope.launch`** → Ejecuta la coroutine en el scope del ViewModel. Si el ViewModel se destruye (por ejemplo, el usuario cierra la pantalla), la coroutine se cancela automáticamente.

---

#### `ViewModelPlato.kt` — ViewModel de platos

**¿Qué hace?** Obtiene platos de una categoría específica desde `GET /api/platos/{id}`.

**Optimización — caché por categoría:**
```kotlin
private var ultimaCategoriaId: Int = -1

fun obtenerPlatosPorCategoria(idCategoria: Int) {
    if (idCategoria == ultimaCategoriaId && _platos.value != null) return  // Ya los tengo
    ultimaCategoriaId = idCategoria
    realizarLlamada(idCategoria, intentos = 0)
}
```
Si el usuario abre la misma categoría dos veces, no hace otra llamada al servidor.

---

#### `ViewModelPlatoDetalle.kt` — ViewModel de extras del plato

**¿Qué hace?** Carga tamaños, sabores y adiciones desde `GET /api/extras`.

**Una sola llamada, tres LiveData:**
```kotlin
val response = api.obtenerExtras()          // Una sola petición HTTP
_tamanos.value = body.tamanos               // LiveData de tamaños
_sabores.value = body.sabores               // LiveData de sabores
_adiciones.value = body.adiciones           // LiveData de adiciones
```
El endpoint `/api/extras` devuelve un JSON con 3 arrays. El ViewModel los separa en 3 LiveData independientes para que cada sección de la UI se actualice por separado.

---

#### `ViewModelReserva.kt` — ViewModel de reservas

**¿Qué hace?** Gestiona dos operaciones:
1. **Cargar temáticas** → `GET /api/tematicas` (puerto 5005)
2. **Crear reserva** → `POST /api/reservas` (puerto 5005)

**LiveData expuestos:**
- `tematicas` → Lista de temáticas para el selector
- `reservaExitosa` → Respuesta exitosa con QR (activa la navegación a confirmación)
- `error` → Mensaje de error para Toast
- `enviando` → Boolean que controla el estado del botón

---

#### `ViewModelDomicilio.kt` — ViewModel de domicilios

**¿Qué hace?** Envía el pedido a domicilio vía `POST /api/domicilios` (puerto 5004).

**Estructura idéntica** a ViewModelReserva: LiveData de pedidoExitoso, error, enviando + reintento automático.

---

### 🔷 5.3 SERVICES (Capa de red — Retrofit)

---

#### `ConexionServiceMenu.kt` — API de menú (puerto 5001)

**¿Qué es?** Una interfaz de Kotlin que Retrofit convierte automáticamente en una clase que hace llamadas HTTP reales.

**Endpoints definidos:**
```kotlin
@GET("api/categorias")
suspend fun obtenerCategorias(): Response<List<ModelCategoria>>

@GET("api/platos/{id_categoria}")
suspend fun obtenerPlatosPorCategoria(@Path("id_categoria") idCategoria: Int): Response<List<ModelPlato>>

@GET("api/extras")
suspend fun obtenerExtras(): Response<ModelExtras>
```

**¿Qué significa `suspend fun`?** Es una función de coroutine — se ejecuta en segundo plano sin bloquear la UI. El `viewModelScope.launch` del ViewModel se encarga de llamarla correctamente.

**¿Qué significa `Response<T>`?** Retrofit envuelve la respuesta HTTP en un objeto `Response` que nos da acceso a:
- `response.isSuccessful` → ¿Fue HTTP 200?
- `response.body()` → Los datos deserializados (Gson convierte JSON → Kotlin)
- `response.code()` → El código HTTP (200, 404, 500, etc.)
- `response.errorBody()` → El cuerpo de error si falló

**Companion object — Configuración de red:**
```kotlin
companion object {
    const val BASE_URL = "http://147.182.238.195:5001/"
    const val BASE_URL_IMAGENES = "http://147.182.238.195:5000/static/img/"

    private val okHttpClient by lazy { ... }  // Se crea UNA sola vez (lazy = perezoso)
    val instance by lazy { ... }              // Singleton de la interfaz
}
```

**¿Por qué `ConnectionPool(0, 1, TimeUnit.NANOSECONDS)` y `Connection: close`?**
Flask (el backend Python) tiene problemas con conexiones HTTP persistentes (keep-alive). Si las dejamos abiertas, obtenemos errores "unexpected end of stream". La solución es:
1. Pool de conexiones vacío (0 conexiones reutilizables)
2. Header `Connection: close` (le dice al servidor que cierre la conexión después de cada respuesta)

---

#### `ConexionServiceReserva.kt` — API de reservas (puerto 5005)

**Endpoints:**
```kotlin
@GET("api/tematicas")
suspend fun obtenerTematicas(): Response<List<ModelTematica>>

@POST("api/reservas")
suspend fun crearReserva(@Body body: ReservaRequest): Response<ReservaResponse>
```

**Interceptor de logging** — imprime en Logcat el método HTTP, URL, body enviado y respuesta recibida. Útil para depurar en desarrollo.

**Timeouts más amplios:** `readTimeout = 30s` porque `POST /api/reservas` genera un QR y envía un correo, lo que puede demorar más que una consulta simple.

---

#### `ConexionServiceDomicilio.kt` — API de domicilios (puerto 5004)

**Endpoint:**
```kotlin
@POST("api/domicilios")
suspend fun crearDomicilio(@Body body: DomicilioRequest): Response<DomicilioResponse>
```

Configuración idéntica a ConexionServiceReserva (logging interceptor + anti-Flask).

---

### 🔷 5.4 MODELS (Modelos de datos)

---

#### `ModelCategoria.kt`
```kotlin
data class ModelCategoria(
    @SerializedName("id")     val id: Int,
    @SerializedName("nombre") val nombre: String,
    @SerializedName("tamano") val tamano: String,
    @SerializedName("imagen") val imagen: String
)
```
**`@SerializedName("id")`** → Le dice a Gson: "cuando encuentres la clave `id` en el JSON, mapéala a esta propiedad". Los nombres deben coincidir EXACTAMENTE con el JSON del backend Flask.

**`data class`** → Kotlin genera automáticamente `toString()`, `equals()`, `hashCode()` y `copy()`.

#### `ModelPlato.kt`
Representa un plato: id, nombre, descripción, precio, imagen (filename o URL), categoriaId.

#### `ModelExtras.kt`
Contenedor que agrupa 3 listas: `tamanos`, `adiciones`, `sabores`. Corresponde a la respuesta de `/api/extras`.

#### `ModelTamano.kt`
Incluye `limiteSabores: Int` — indica cuántos sabores permite ese tamaño (si es > 1, se muestra la sección de sabores).

#### `ModelReserva.kt` — Modelos de request y response para reservas
- **`ReservaRequest`** → Body del POST. Contiene 3 objetos: `ClienteReserva`, `DatosReserva`, `List<PedidoItem>`
- **`ClienteReserva`** → doc, nom, correo, tel (nombres cortos que el backend Flask espera)
- **`DatosReserva`** → fec, hor, tematica, personas, piso, metodo_pago
- **`PedidoItem`** → id del producto, cantidad, precio
- **`ReservaResponse`** → status, message, qr (base64), id

#### `ModelDomicilio.kt` — Modelos para domicilio
- **`DomicilioRequest`** → cliente, domicilio, productos
- **`DatosDomicilio`** → direccion, metodo_pago
- **`DomicilioResponse`** → Es un `typealias` de `ReservaResponse` (misma estructura)

---

### 🔷 5.5 ADAPTERS (RecyclerView)

---

#### `AdapterCategoria.kt`

**¿Qué es un Adapter?** Es el puente entre los datos (lista de categorías) y el RecyclerView (la grilla visual).

**Constructor:**
```kotlin
class AdapterCategoria(
    private var listaCategorias: List<ModelCategoria>,
    private val onCategoriaClick: (ModelCategoria) -> Unit   // Lambda de clic
)
```

**ViewHolder:** Mantiene las referencias a las vistas de CADA item (`tvNombreCategoria`, `tvTamanoCategoria`, `imgCategoria`).

**3 funciones obligatorias:**
1. `onCreateViewHolder` → Infla el layout `item_menu.xml` para crear la vista de cada item
2. `onBindViewHolder` → Asigna los datos a las vistas: nombre, tamaño, imagen (con Glide) y el listener de clic
3. `getItemCount` → Devuelve el total de items

**`actualizarLista()`** → Reemplaza la lista y llama a `notifyDataSetChanged()` para refrescar la UI.

#### `AdapterPlato.kt`
Mismo patrón. Usa `notifyItemRangeRemoved` + `notifyItemRangeInserted` en vez de `notifyDataSetChanged` para una animación más suave.

---

## 📌 6. TECNOLOGÍAS Y LIBRERÍAS UTILIZADAS

| Tecnología | ¿Para qué se usa? |
|---|---|
| **Kotlin** | Lenguaje de programación principal |
| **ViewBinding** | Acceso seguro a vistas sin `findViewById` (genera clases automáticas) |
| **Retrofit 2** | Cliente HTTP para consumir APIs REST |
| **Gson** | Convertir JSON ↔ objetos Kotlin automáticamente |
| **OkHttp** | Cliente HTTP subyacente de Retrofit (configurar timeouts, interceptors) |
| **Glide** | Cargar imágenes desde URLs con caché y placeholders |
| **LiveData** | Datos observables que respetan el ciclo de vida de la Activity |
| **ViewModel** | Almacena datos que sobreviven rotaciones de pantalla |
| **Coroutines** | Programación asíncrona sin bloquear la UI |
| **Material Design** | Componentes visuales (TextInputLayout, MaterialButton, CardView) |
| **MediaStore** | Guardar imágenes en la galería del dispositivo |
| **ActivityResultContracts** | Seleccionar archivos de la galería (nuevo API, reemplaza startActivityForResult) |

---

## 📌 7. CONCEPTOS CLAVE PARA LA SUSTENTACIÓN

### ¿Qué es ViewBinding?
En vez de usar `findViewById(R.id.btnReservar)`, usamos `binding.btnReservar`. ViewBinding genera una clase automática por cada layout XML que tiene propiedades tipadas para cada vista con `id`. Es más seguro porque si el `id` no existe, da error en tiempo de compilación (no en ejecución).

### ¿Qué es una Coroutine?
Es una forma de ejecutar código en segundo plano sin bloquear la pantalla. `viewModelScope.launch { }` crea una coroutine que:
- Se ejecuta en el hilo principal por defecto
- Puede "pausarse" en funciones `suspend` (como las de Retrofit) sin congelar la UI
- Se cancela automáticamente si el ViewModel se destruye

### ¿Qué es LiveData?
Es un contenedor de datos que la Activity puede observar. Cuando el dato cambia, la UI se actualiza automáticamente. Además, solo notifica a Activities que están activas (respeta el ciclo de vida → no causa crashes).

### ¿Qué son los principios SOLID aplicados?
- **SRP** (Single Responsibility): Cada clase tiene una sola responsabilidad. Ej: `ViewModelCategoria` solo maneja categorías.
- **OCP** (Open/Closed): `BaseActivity` está abierta a extensión (herencia) pero cerrada a modificación.
- **ISP** (Interface Segregation): Cada servicio Retrofit solo tiene los endpoints que le corresponden.
- **DIP** (Dependency Inversion): Los ViewModels dependen de interfaces Retrofit, no de implementaciones concretas.

### ¿Cómo funciona el reintento automático?
Si la llamada al servidor falla (timeout, sin internet), la función se llama a sí misma con `intentos + 1`. Si ya falló 3 veces (intentos 0, 1, 2), muestra un error al usuario.

### ¿Por qué usamos `finishAffinity()` después de crear la reserva?
Cuando la reserva se crea exitosamente y navegamos a la pantalla de confirmación, llamamos `finishAffinity()` para cerrar TODAS las activities anteriores (carrito, datos cliente, reserva). Así el usuario no puede volver atrás y re-enviar la reserva.

---

## 📌 8. COMUNICACIÓN APP ↔ BACKEND

### JSON que envía la app al crear una RESERVA:
```json
{
  "cliente": {
    "doc": "1234567890",
    "nom": "David López",
    "correo": "david@email.com",
    "tel": "3001234567"
  },
  "reserva": {
    "fec": "2026-03-25",
    "hor": "18",
    "tematica": 2,
    "personas": 4,
    "piso": 1,
    "metodo_pago": "transferencia"
  },
  "pedido": [
    { "id": 5, "cantidad": 2, "precio": 29000 }
  ]
}
```

### JSON que responde el servidor:
```json
{
  "status": "success",
  "message": "Reserva creada con éxito con ID #42",
  "id": 42,
  "qr": "iVBORw0KGgoAAAANSUhEUgAA..."   ← imagen QR en base64
}
```

### JSON del DOMICILIO:
```json
{
  "cliente": { "doc": "...", "nom": "...", "correo": "...", "tel": "..." },
  "domicilio": {
    "direccion": "Calle 10 #12-34 - Barrio Centro",
    "metodo_pago": "efectivo"
  },
  "productos": [
    { "id": 5, "cantidad": 2, "precio": 29000 }
  ]
}
```

---

## 📌 9. COMANDOS DE BUILD

```bash
# Compilar el APK de debug
./gradlew assembleDebug

# Instalar en dispositivo/emulador
./gradlew installDebug

# Ejecutar tests unitarios
./gradlew test
```

**SDK:** compileSdk = 36, minSdk = 24, targetSdk = 36, JVM target 11.

---

## 📌 10. RESUMEN RÁPIDO (CHEAT SHEET)

| Pregunta | Respuesta |
|---|---|
| ¿Qué patrón usas? | MVVM (Model-View-ViewModel) |
| ¿Cómo haces las llamadas al API? | Retrofit con coroutines (`suspend fun`) |
| ¿Cómo comunicas ViewModel → Activity? | LiveData (`observe`) |
| ¿Cómo comunicas Activity → Activity? | Intent extras (`putExtra` / `getExtra`) |
| ¿Cómo cargas imágenes? | Glide desde URL del servidor (puerto 5000) |
| ¿Cómo manejas errores de red? | Reintento automático (máx. 2) + Toast |
| ¿Cómo accedes a las vistas? | ViewBinding (`binding.btnReservar`) |
| ¿Cómo serializas/deserializas JSON? | Gson con `@SerializedName` |
| ¿Cuántos microservicios Flask? | 3 (menú:5001, reservas:5005, domicilios:5004) |
| ¿Cómo manejas el QR? | Base64 → Bitmap → ImageView + descarga con MediaStore |
| ¿Cómo validas formularios? | Funciones privadas que validan campo por campo y retornan Boolean |
| ¿Por qué `Connection: close`? | Flask tiene problemas con keep-alive |

---

*Documento generado para la sustentación del proyecto MovilTPR — SENA 2026*

