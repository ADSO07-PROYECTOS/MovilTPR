# AGENTS.md — ReservasMovil (MovilTPR)

## Project Overview

Android app (Kotlin) for **Restaurante Tres Pasos** — a restaurant reservation and ordering system. Users browse food categories, select dishes with sizes/flavors/additions, add to cart, enter client data, and create a reservation via a Flask backend. The server returns a QR code for confirmation.

## Architecture

- **MVVM con paquetes separados**: `models/`, `viewmodels/`, `services/`, `adapters/`, `views/`.
- **UI Layer**: Activities + XML layouts (View system con **ViewBinding** y **DataBinding** — **no Compose**). Todas las Activities extienden `BaseActivity`, que configura la barra de navegación inferior con 3 botones: Casa (→ `MainActivity`), Carrito (→ `CarritoActivity`), Mis Reservas (→ `MisReservasActivity`).
- **ViewModel → Service (Retrofit suspend)**: Los ViewModels en `viewmodels/` llaman directamente a las interfaces de `services/` usando **coroutines** (`viewModelScope.launch`). No hay capa de Repository. Todos los ViewModels implementan **reintento automático (máx. 2)** ante fallos de red.
- **Tres servicios Retrofit** (todos apuntan a backends Flask):
  - `ConexionServiceMenu` → `http://147.182.238.195:5001/` — menú (`/api/categorias`, `/api/platos/{id}`, `/api/extras`)
  - `ConexionServiceReserva` → `http://147.182.238.195:5005/` — reservas (`/api/tematicas`, `POST /api/reservas`, `GET /api/reservas/{cedula}`, `PUT /api/reservas/{id}`, `DELETE /api/reservas/{id}`)
  - `ConexionServiceDomicilio` → `http://147.182.238.195:5004/` — domicilios (`POST /api/domicilios`)
  - Imágenes estáticas desde puerto `5000`: `ConexionServiceMenu.BASE_URL_IMAGENES`
- **URL base y cliente Retrofit** definidos en el `companion object` de cada interfaz de servicio.
- **Comunicación inter-Activity**: vía `Intent` extras con prefijos `CARRITO_*` y `CLI_*` — no hay singleton de carrito ni base de datos local.

## User Flow (Activity chain)

```
MainActivity (grilla de categorías)
  → VerPlatosActivity (lista de platos)
    → PlatoDetalleActivity (selector de tamaño/sabor/adiciones)
      → CarritoActivity (ajuste de cantidad, diálogo: restaurante vs domicilio)
        ├─ Restaurante → DatosClienteActivity → ReservaActivity → ConfirmacionReservaActivity (QR)
        └─ Domicilio   → DatosClienteActivity → DomicilioActivity → ConfirmacionReservaActivity (QR)

MisReservasActivity (buscar reservas por cédula, ver QR, editar, eliminar)
  └─ Accesible desde la barra de navegación inferior en cualquier pantalla
```

## Key Conventions

- **Package**: `com.sena.myapplication`. Namespace en `app/build.gradle.kts`.
- **Nomenclatura del instructor Diego Pinilla**:
  - Modelos: `ModelCategoria`, `ModelPlato`, `ModelExtras`, etc.
  - Servicios: `ConexionServiceMenu`, `ConexionServiceReserva`, `ConexionServiceDomicilio`
  - ViewModels: `ViewModelCategoria`, `ViewModelPlato`, `ViewModelPlatoDetalle`, `ViewModelReserva`, `ViewModelDomicilio`, `ViewModelMisReservas`
  - Adapters: `AdapterCategoria`, `AdapterPlato`
- **Models** usan `@SerializedName` para Gson — los nombres de campo deben coincidir exactamente con las claves JSON del API Flask.
- **Español en todo**: UI strings, variables, clases, comentarios.
- **ViewBinding**: Las Activities usan `XxxBinding.inflate(layoutInflater)` en lugar de `findViewById`.
- **Coroutines**: Las funciones de servicio son `suspend fun` que retornan `Response<T>`. Los ViewModels las llaman con `viewModelScope.launch` + `try/catch`.
- **LiveData encapsulado**: `_mutableLiveData` privado + `liveData` público.
- **Adapters** reciben un lambda `onXxxClick` para el clic en items. ViewHolder usa `findViewById`.
- **OkHttp**: Los tres clientes fuerzan `Connection: close` y usan connection pool vacío para evitar problemas con Flask. Los tres incluyen un logging interceptor para depuración (tags Logcat: `MenuAPI`, `ReservaAPI`, `DomicilioAPI`).

## Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install en dispositivo/emulador conectado
./gradlew installDebug

# Tests unitarios
./gradlew test

# Tests instrumentados
./gradlew connectedAndroidTest
```

- **SDK**: `compileSdk = 36`, `minSdk = 24`, `targetSdk = 36`, JVM target `11`.
- **Version catalog**: `gradle/libs.versions.toml` para deps catalogadas; Retrofit, Glide, Lifecycle y Coroutines están declaradas inline en `app/build.gradle.kts`.
- Cleartext traffic habilitado (`android:usesCleartextTraffic="true"`) — el backend usa HTTP.

## Package Structure

```
com.sena.myapplication/
  models/          → Data classes con @SerializedName (ModelCategoria, ModelPlato, etc.)
  services/        → Interfaces Retrofit + companion object con Retrofit instance
  viewmodels/      → ViewModels con coroutines, LiveData y reintento automático
  adapters/        → RecyclerView Adapters con lambda de click
  views/           → Activities (todas extienden BaseActivity)
```

## Important Files

| Propósito | Ruta |
|---|---|
| Servicio de menú (port 5001) | `services/ConexionServiceMenu.kt` |
| Servicio de reservas (port 5005) | `services/ConexionServiceReserva.kt` |
| Servicio de domicilios (port 5004) | `services/ConexionServiceDomicilio.kt` |
| Modelos de datos | `models/Model*.kt` + `ModelReserva.kt` (request/response) + `ModelDomicilio.kt` + `ModelMiReserva.kt` |
| ViewModels con coroutines | `viewmodels/ViewModelCategoria.kt`, `ViewModelPlato.kt`, `ViewModelPlatoDetalle.kt`, `ViewModelReserva.kt`, `ViewModelDomicilio.kt`, `ViewModelMisReservas.kt` |
| Adapters | `adapters/AdapterCategoria.kt`, `AdapterPlato.kt` |
| Base de navegación | `views/BaseActivity.kt` |
| Layouts | `res/layout/activity_*.xml`, `item_*.xml`, `plato_detalle.xml`, `dialog_*.xml` |

## When Adding Features

- **Nuevas Activities**: Crear en `views/`, extender `BaseActivity`, llamar `configurarBarraNavegacion()` después de `setContentView`. Registrar en `AndroidManifest.xml` como `.views.NombreActivity`.
- **Nuevos endpoints**: Agregar `suspend fun` en `ConexionServiceMenu.kt` (menú), `ConexionServiceReserva.kt` (reservas) o `ConexionServiceDomicilio.kt` (domicilios).
- **Nuevos ViewModels**: Crear en `viewmodels/`, usar `viewModelScope.launch` con `try/catch` y reintento (máx. 2). Exponer `_mutableLiveData` privado y `liveData` público.
- **Nuevos modelos**: Crear en `models/` con prefijo `Model`. Usar `@SerializedName` que coincida con JSON del API Flask.
- **Imágenes**: Si el API devuelve solo un filename, usar `ConexionServiceMenu.BASE_URL_IMAGENES + filename` y cargar con Glide.
- **Datos entre Activities**: Usar extras del Intent con prefijos descriptivos (`CARRITO_*`, `CLI_*`, etc.).
