# 📱 Sistema Reactivo - Versión Básica

## 🎯 Concepto

Un sistema que usa **LiveData + ViewModel + Observable** para que la UI se actualice automáticamente cuando los datos cambian.

---

## 🏗️ Arquitectura Simple

```
Servidor
    ↓
Retrofit (API)
    ↓
Repository (obtiene datos)
    ↓
LiveData (almacena datos)
    ↓
ViewModel (gestiona datos)
    ↓
Activity (observa cambios)
    ↓
RecyclerView (muestra datos)
```

---

## 📁 Archivos Principales

### 1. **RepositoryCategorias.kt** (CAPA DE DATOS)
```kotlin
class RepositoryCategorias {
    fun obtenerCategorias(): LiveData<List<Categoria>> {
        val categoriasLiveData = MutableLiveData<List<Categoria>>()
        
        // Llamada a la API
        api.obtenerCategorias().enqueue(object : Callback<List<Categoria>> {
            override fun onResponse(...) {
                // Cuando llegan datos
                categoriasLiveData.postValue(datos)  // ← Notifica cambio
            }
        })
        
        return categoriasLiveData
    }
}
```

**Responsabilidad:** Obtener datos del servidor

---

### 2. **CategoriaViewModel.kt** (GESTIÓN DE DATOS)
```kotlin
class CategoriaViewModel : ViewModel() {
    private val repository = RepositoryCategorias()
    
    fun obtenerCategorias(): LiveData<List<Categoria>> {
        return repository.obtenerCategorias()
    }
}
```

**Responsabilidad:** Gestionar ciclo de vida y datos

---

### 3. **MainActivity.kt** (VISTA - OBSERVABLE)
```kotlin
class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: CategoriaViewModel
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = ViewModelProvider(this).get(CategoriaViewModel::class.java)
        
        // ← OBSERVABLE: Se ejecuta cuando los datos cambian
        viewModel.obtenerCategorias().observe(this) { categorias ->
            adapter = CategoriaAdapter(categorias)
            rvCategorias.adapter = adapter
        }
    }
}
```

**Responsabilidad:** Observar cambios y actualizar UI

---

## 🔄 Flujo de Ejecución

```
┌─────────────────────┐
│ MainActivity.onCreate│
└──────────┬──────────┘
           ↓
┌─────────────────────────────────┐
│ Crear ViewModel                 │
│ viewModel = ViewModelProvider()│
└──────────┬──────────────────────┘
           ↓
┌────────────────────────────────────────┐
│ Configurar observe()                   │
│ viewModel.obtenerCategorias()         │
│    .observe(this) { categorias -> ... │
└──────────┬─────────────────────────────┘
           ↓
┌─────────────────────────────────┐
│ Repository hace llamada a API  │
│ api.obtenerCategorias()        │
└──────────┬──────────────────────┘
           ↓
┌─────────────────────────────────┐
│ Llegan datos del servidor       │
└──────────┬──────────────────────┘
           ↓
┌─────────────────────────────────┐
│ LiveData.postValue(datos)       │ ← Notifica
└──────────┬──────────────────────┘
           ↓
┌─────────────────────────────────┐
│ observe() se ejecuta            │
│ { categorias -> actualizar UI } │ ← Callback
└──────────┬──────────────────────┘
           ↓
┌─────────────────────────────────┐
│ RecyclerView se actualiza ✨    │
└─────────────────────────────────┘
```

---

## 💡 Conceptos Clave

### **LiveData**
```kotlin
val categoriasLiveData = MutableLiveData<List<Categoria>>()

// Notificar cambios
categoriasLiveData.postValue(listaNuevaDeCategoías)

// Ahora todos los observadores reciben notificación ✨
```

### **Observable (observe)**
```kotlin
viewModel.obtenerCategorias().observe(this) { categorias ->
    // Se ejecuta CADA VEZ que categoriasLiveData cambia
    adapter.actualizar(categorias)
}
```

### **ViewModel**
```kotlin
class CategoriaViewModel : ViewModel() {
    // Sobrevive a rotaciones de pantalla
    // Gestiona datos del ciclo de vida
}
```

---

## 📊 Ventajas

✅ **Automático** - UI se actualiza automáticamente  
✅ **Reactivo** - Observable detecta cambios  
✅ **Persistente** - Datos persisten en rotaciones  
✅ **Limpio** - Separación de responsabilidades  
✅ **Seguro** - Thread-safe  

---

## 🧪 Cómo Funciona

### **Ejemplo: Cargar Categorías**

**MainActivity:**
```kotlin
viewModel.obtenerCategorias().observe(this) { categorias ->
    // Mostrar categorías en RecyclerView
}
```

**ViewModel:**
```kotlin
fun obtenerCategorias(): LiveData<List<Categoria>> {
    return repository.obtenerCategorias()
}
```

**Repository:**
```kotlin
fun obtenerCategorias(): LiveData<List<Categoria>> {
    val data = MutableLiveData<List<Categoria>>()
    
    // Llamar API
    api.obtenerCategorias().enqueue(object : Callback<List<Categoria>> {
        override fun onResponse(call, response) {
            data.postValue(response.body()!!)  // ← Notificar
        }
    })
    
    return data
}
```

---

## 📈 Patrón: Observer

```
Cambio en datos
        ↓
LiveData.postValue(nuevosDatos)
        ↓
Notifica a observadores
        ↓
observe() se ejecuta
        ↓
UI se actualiza ✨
```

---

## 🎓 Para Sustentación

**Explicar así:**

1. **Repository** obtiene datos del servidor
2. **LiveData** almacena los datos y notifica cambios
3. **ViewModel** gestiona los datos del ciclo de vida
4. **Activity** observa los cambios con `observe()`
5. Cuando hay cambios, **RecyclerView** se actualiza automáticamente

**Patrón:** Observer Pattern + Reactive Programming

---

## 🔧 Estructura de Carpetas

```
com.sena.myapplication/
├── MainActivity.kt          (Activity - Vista)
├── VerPlatosActivity.kt    (Activity - Vista)
├── MenuGeneral.kt          (Activity - Vista)
│
├── CategoriaViewModel.kt   (ViewModel - Gestión)
├── PlatoViewModel.kt       (ViewModel - Gestión)
│
├── RepositoryCategorias.kt (Repository - Datos)
├── RepositoryPlatos.kt     (Repository - Datos)
│
├── Categoria.kt            (Model - Datos)
├── Plato.kt                (Model - Datos)
│
├── CategoriaAdapter.kt     (RecyclerView)
├── PlatoAdapter.kt         (RecyclerView)
│
├── ApiService.kt           (Interface Retrofit)
└── RetrofitClient.kt       (Cliente HTTP)
```

---

## 📚 Dependencias

```gradle
// ViewModel y LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")
implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.1")

// Retrofit (ya existía)
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")
```

---

## ✅ Resumen

| Componente | Responsabilidad |
|-----------|-----------------|
| **Repository** | Obtener datos del servidor |
| **LiveData** | Almacenar datos y notificar cambios |
| **ViewModel** | Gestionar ciclo de vida de datos |
| **Activity** | Observar cambios y actualizar UI |

---

## 🎯 Patrón: Model-View-ViewModel (MVVM)

```
┌─────────────────────────────────┐
│ MODEL (Datos)                   │
│ - Repository                    │
│ - LiveData                      │
└────────────┬────────────────────┘
             ↓
┌─────────────────────────────────┐
│ VIEW MODEL (Lógica)             │
│ - CategoriaViewModel            │
│ - PlatoViewModel                │
└────────────┬────────────────────┘
             ↓
┌─────────────────────────────────┐
│ VIEW (UI)                       │
│ - MainActivity                  │
│ - VerPlatosActivity             │
│ - RecyclerView                  │
└─────────────────────────────────┘
```

---

## 🚀 ¡Listo!

Sistema reactivo simple, fácil de explicar y basado en:
- ✅ LiveData (Observable)
- ✅ ViewModel (Gestión de datos)
- ✅ Repository Pattern (Separación de capas)
- ✅ Observer Pattern (Reactive)

**Sin polling, sin complejidad innecesaria.** ✨

