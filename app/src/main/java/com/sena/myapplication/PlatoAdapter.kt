package com.sena.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PlatoAdapter(
    private var listaPlatos: List<Plato>,
    private val onPlatoClick: (Plato) -> Unit
) : RecyclerView.Adapter<PlatoAdapter.PlatoViewHolder>() {

    class PlatoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNombrePlato: TextView     = itemView.findViewById(R.id.tvNombrePlato)
        val tvDescripcionPlato: TextView = itemView.findViewById(R.id.tvDescripcionPlato)
        val tvPrecioPlato: TextView     = itemView.findViewById(R.id.tvPrecioPlato)
        val btnSeleccionar: Button      = itemView.findViewById(R.id.btnSeleccionar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlatoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_plato, parent, false)
        return PlatoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlatoViewHolder, position: Int) {
        val plato = listaPlatos[position]
        holder.tvNombrePlato.text      = plato.nombre
        holder.tvDescripcionPlato.text = plato.descripcion
        holder.tvPrecioPlato.text      = holder.itemView.context
            .getString(R.string.precio_total_label, plato.precio.toInt())
        holder.btnSeleccionar.setOnClickListener { onPlatoClick(plato) }
    }

    override fun getItemCount() = listaPlatos.size

    fun actualizarLista(nuevaLista: List<Plato>) {
        val oldSize = listaPlatos.size
        listaPlatos = nuevaLista
        if (oldSize > 0) notifyItemRangeRemoved(0, oldSize)
        if (nuevaLista.isNotEmpty()) notifyItemRangeInserted(0, nuevaLista.size)
    }
}