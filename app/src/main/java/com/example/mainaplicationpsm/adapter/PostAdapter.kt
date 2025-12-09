package com.example.mainaplicationpsm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.Post

class PostAdapter(
    private var postList: MutableList<Post>, // Debe ser MutableList para poder editarla
    private val currentUserId: Int,
    private val onAction: (Post, ActionType) -> Unit
) : RecyclerView.Adapter<PostViewHolder>() {

    enum class ActionType { EDIT, DELETE, OPEN_COMMENTS, TOGGLE_FAVORITE, TOGGLE_LIKE }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PostViewHolder(layoutInflater.inflate(R.layout.item_post, parent, false))
    }

    override fun getItemCount(): Int = postList.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = postList[position]
        holder.render(item)

        // --- LISTENERS ---

        // Clic en el globo de comentarios o en el contador
        holder.ivComment.setOnClickListener { onAction(item, ActionType.OPEN_COMMENTS) }
        holder.tvCommentCount.setOnClickListener { onAction(item, ActionType.OPEN_COMMENTS) }

        // Clic en el corazón (Like)
        holder.ivLike.setOnClickListener { onAction(item, ActionType.TOGGLE_LIKE) }

        // Clic en favoritos
        holder.ivFavorite.setOnClickListener { onAction(item, ActionType.TOGGLE_FAVORITE) }

        // --- MENÚ DE OPCIONES (Solo si eres el dueño) ---
        val btnOptions = holder.itemView.findViewById<View>(R.id.ivOptions)
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

    // --- FUNCIONES AUXILIARES (Estas son las que te faltaban) ---

    // 1. Obtener índice (Soluciona el error en rojo)
    fun getPostIndex(post: Post): Int {
        return postList.indexOfFirst { it.id == post.id }
    }

    // 2. Añadir posts al final (Para el scroll infinito)
    fun addPosts(newPosts: List<Post>) {
        val startPosition = postList.size
        postList.addAll(newPosts)
        notifyItemRangeInserted(startPosition, newPosts.size)
    }

    // 3. Limpiar lista (Para refrescar o recargar)
    fun clear() {
        postList.clear()
        notifyDataSetChanged()
    }

    // 4. Eliminar un post específico visualmente
    fun removePost(post: Post) {
        val index = getPostIndex(post)
        if (index != -1) {
            postList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}