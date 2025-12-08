package com.example.mainaplicationpsm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.Post

class PostAdapter(
    private var postList: MutableList<Post>,
    private val currentUserId: Int,  // ID para saber si es TU post
    private val onAction: (Post, ActionType) -> Unit // Callback único para todo
) : RecyclerView.Adapter<PostViewHolder>() {

    // Definimos las 3 acciones posibles
    enum class ActionType { EDIT, DELETE, OPEN_COMMENTS, TOGGLE_FAVORITE, TOGGLE_LIKE }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PostViewHolder(layoutInflater.inflate(R.layout.item_post, parent, false))
    }

    override fun getItemCount(): Int = postList.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = postList[position]
        holder.render(item)

        // --- 1. LÓGICA DE COMENTARIOS (Clic en el ícono de globo) ---
        // Al tocar el icono o el contador, abrimos el detalle
        holder.ivComment.setOnClickListener { onAction(item, ActionType.OPEN_COMMENTS) }
        holder.tvCommentCount.setOnClickListener { onAction(item, ActionType.OPEN_COMMENTS) }
        holder.ivFavorite.setOnClickListener { onAction(item, ActionType.TOGGLE_FAVORITE) }
        holder.ivLike.setOnClickListener { onAction(item, ActionType.TOGGLE_LIKE) }

        // --- 2. LÓGICA DE EDICIÓN/BORRADO (Clic en opciones) ---
        // Buscamos el botón de opciones (flecha/tres puntos)
        val btnOptions = holder.itemView.findViewById<View>(R.id.ivOptions)

        // Solo mostramos el botón si el post es tuyo
        if (item.userId == currentUserId) {
            btnOptions.visibility = View.VISIBLE
            btnOptions.setOnClickListener { view ->
                showPopupMenu(view, item)
            }
        } else {
            btnOptions.visibility = View.GONE
        }
    }

    private fun showPopupMenu(view: View, post: Post) {
        val popup = PopupMenu(view.context, view)
        popup.menu.add(0, 1, 0, "Editar")
        popup.menu.add(0, 2, 0, "Eliminar")

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                1 -> onAction(post, ActionType.EDIT)
                2 -> onAction(post, ActionType.DELETE)
            }
            true
        }
        popup.show()
    }

    fun removePost(post: Post) {
        val index = postList.indexOfFirst { it.id == post.id }
        if (index != -1) {
            postList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}