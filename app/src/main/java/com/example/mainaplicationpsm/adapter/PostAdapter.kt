package com.example.mainaplicationpsm.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.Post

// Notarás que el constructor cambió: ahora recibe 'currentUserId' y 'onAction'
class PostAdapter(
    private var postList: MutableList<Post>,
    private val currentUserId: Int,
    private val onAction: (Post, ActionType) -> Unit
) : RecyclerView.Adapter<PostViewHolder>() {

    // Esta enumeración es necesaria para que el Fragment sepa qué opción elegiste
    enum class ActionType { EDIT, DELETE, OPEN_DETAIL }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return PostViewHolder(layoutInflater.inflate(R.layout.item_post, parent, false))
    }

    override fun getItemCount(): Int = postList.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val item = postList[position]
        holder.render(item)

        holder.comments.setOnClickListener {
            // Ojo: holder.comments es el TextView contador,
            // asegúrate de tener referencia al ImageView (ivComment) en el ViewHolder también
            onAction(item, ActionType.OPEN_DETAIL)
        }
        // --- LÓGICA DE PROPIETARIO ---
        // Buscamos el botón de opciones (tres puntos) en el item_post.xml
        val btnOptions = holder.itemView.findViewById<View>(R.id.ivOptions)

        // Solo mostramos el botón si el usuario logueado es el dueño del post
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
        // Creamos el menú por código: ID, orden, ID recurso, Texto
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

    // Función auxiliar para borrar visualmente el post sin recargar la pantalla
    fun removePost(post: Post) {
        val index = postList.indexOfFirst { it.id == post.id }
        if (index != -1) {
            postList.removeAt(index)
            notifyItemRemoved(index)
        }
    }
}