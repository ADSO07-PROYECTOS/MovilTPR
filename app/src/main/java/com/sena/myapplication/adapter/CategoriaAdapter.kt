package com.sena.myapplication.adapter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sena.myapplication.R
import com.sena.myapplication.models.CategoriaModel

class CategoriaAdapter(
  private val listaCategoriaModels: List<CategoriaModel>,
  private val onCategoriaClick: (CategoriaModel) -> Unit // Función que escucha el clic
) : RecyclerView.Adapter<CategoriaAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombreCategoria: TextView = itemView.findViewById(R.id.tvNombreCategoria)
        val tvTamanoCategoria: TextView = itemView.findViewById(R.id.tvTamanoCategoria)
        val imgCategoria: ImageView = itemView.findViewById(R.id.imgCategoria)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_menu, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val categoria = listaCategoriaModels[position]

        holder.tvNombreCategoria.text = categoria.nombre
        holder.tvTamanoCategoria.text = categoria.tamano


        val urlImagen = "http://147.182.238.195:5000/static/img/${categoria.imagen}"
        Log.d("CategoriaAdapter", "Cargando imagen para '${categoria.nombre}': $urlImagen")

        Glide.with(holder.itemView.context)
            .load(urlImagen)
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(holder.imgCategoria)


        holder.itemView.setOnClickListener {
            onCategoriaClick(categoria)
        }
    }

    override fun getItemCount(): Int {
        return listaCategoriaModels.size
    }
}
