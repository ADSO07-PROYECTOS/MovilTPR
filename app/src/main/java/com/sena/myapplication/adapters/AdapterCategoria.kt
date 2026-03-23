package com.sena.myapplication.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sena.myapplication.R
import com.sena.myapplication.models.ModelCategoria
import com.sena.myapplication.services.ConexionServiceMenu

/**
 * Adapter para la grilla de categorías en MainActivity.
 *
 * Principio SRP: Solo se encarga de vincular datos de [ModelCategoria]
 * con las vistas del item layout [R.layout.item_menu].
 *
 * Principio OCP: El comportamiento de clic se delega a la lambda
 * [onCategoriaClick], permitiendo cambiar la acción sin modificar el adapter.
 *
 * @param listaCategorias Lista inmutable de categorías a mostrar.
 * @param onCategoriaClick Lambda invocada al hacer clic en una categoría.
 */
class AdapterCategoria(
    private var listaCategorias: List<ModelCategoria>,
    private val onCategoriaClick: (ModelCategoria) -> Unit
) : RecyclerView.Adapter<AdapterCategoria.CategoriaViewHolder>() {

    /** ViewHolder que mantiene las referencias a las vistas del item. */
    class CategoriaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreCategoria: TextView  = itemView.findViewById(R.id.tvNombreCategoria)
        val tvTamanoCategoria: TextView  = itemView.findViewById(R.id.tvTamanoCategoria)
        val imgCategoria: ImageView      = itemView.findViewById(R.id.imgCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_menu, parent, false)
        return CategoriaViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoriaViewHolder, position: Int) {
        val categoria = listaCategorias[position]

        holder.tvNombreCategoria.text = categoria.nombre
        holder.tvTamanoCategoria.text = categoria.tamano

        // Construir URL completa prepending el servidor de imágenes (puerto 5000)
        val urlImagen = "${ConexionServiceMenu.BASE_URL_IMAGENES}${categoria.imagen}"
        Log.d("AdapterCategoria", "Cargando imagen para '${categoria.nombre}': $urlImagen")

        Glide.with(holder.itemView.context)
            .load(urlImagen)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.imgCategoria)

        holder.itemView.setOnClickListener {
            onCategoriaClick(categoria)
        }
    }

    override fun getItemCount(): Int = listaCategorias.size

    /** Actualiza la lista y notifica al RecyclerView del cambio completo. */
    fun actualizarLista(nuevaLista: List<ModelCategoria>) {
        listaCategorias = nuevaLista
        notifyDataSetChanged()
    }
}

