package com.example.mainaplicationpsm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.local.Draft

class DraftAdapter(
    private var drafts: List<Draft>,
    private val onDraftClick: (Draft) -> Unit,
    private val onDeleteClick: (Draft) -> Unit
) : RecyclerView.Adapter<DraftAdapter.DraftViewHolder>() {

    class DraftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.tvTitle)
        val desc: TextView = view.findViewById(R.id.tvDesc)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DraftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_draft, parent, false)
        return DraftViewHolder(view)
    }

    override fun onBindViewHolder(holder: DraftViewHolder, position: Int) {
        val draft = drafts[position]

        // Si el título está vacío, mostramos "Sin título"
        holder.title.text = if (draft.title.isNotEmpty()) draft.title else "(Sin Título)"
        holder.desc.text = draft.description

        // Clic en toda la tarjeta para editar
        holder.itemView.setOnClickListener {
            onDraftClick(draft)
        }

        // Clic en eliminar
        holder.btnDelete.setOnClickListener {
            onDeleteClick(draft)
        }
    }

    override fun getItemCount() = drafts.size

    // Función para actualizar lista tras borrar sin recrear adapter
    fun updateList(newList: List<Draft>) {
        drafts = newList
        notifyDataSetChanged()
    }
}