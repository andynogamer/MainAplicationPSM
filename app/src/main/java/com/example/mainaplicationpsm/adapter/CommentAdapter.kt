package com.example.mainaplicationpsm.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.Comment

class CommentAdapter(
    private var commentList: List<Comment>,
    // Callback actualizado para manejar Reply y Like
    private val onAction: (Comment, ActionType) -> Unit
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    enum class ActionType { REPLY, LIKE } // <--- Agregamos LIKE

    fun updateList(newList: List<Comment>) {
        commentList = newList
        notifyDataSetChanged()
    }

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivCommentAvatar)
        val tvUser: TextView = view.findViewById(R.id.tvCommentUser)
        val tvContent: TextView = view.findViewById(R.id.tvCommentText)
        val tvDate: TextView = view.findViewById(R.id.tvCommentDate)
        val tvLikes: TextView = view.findViewById(R.id.tvCommentLikes)
        val ivLike: ImageView = view.findViewById(R.id.ivCommentLike)
        val tvReply: TextView = view.findViewById(R.id.tvReply)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CommentViewHolder(layoutInflater.inflate(R.layout.item_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]

        // ... (tu código de textos y avatar sigue igual) ...
        holder.tvUser.text = comment.username
        holder.tvContent.text = comment.content
        holder.tvDate.text = "${comment.date} ${comment.time}"
        holder.tvLikes.text = comment.likeCount.toString()

        // --- LÓGICA DE INDENTACIÓN (Mantenla como estaba) ---
        val params = holder.itemView.layoutParams as ViewGroup.MarginLayoutParams
        params.marginStart = if (comment.parentId != null && comment.parentId != 0) 120 else 0
        holder.itemView.layoutParams = params

        // --- LÓGICA DE LIKE VISUAL ---
        if (comment.isLikedByMe) {
            holder.ivLike.setImageResource(R.drawable.ic_heart_filled) // Corazón rojo
            holder.ivLike.setColorFilter(holder.itemView.context.getColor(R.color.secondary_purple_all_4))
        } else {
            holder.ivLike.setImageResource(R.drawable.ic_heart_outline) // Corazón vacío
            holder.ivLike.setColorFilter(holder.itemView.context.getColor(android.R.color.darker_gray))
        }

        // --- LISTENERS ---
        holder.tvReply.setOnClickListener { onAction(comment, ActionType.REPLY) }

        // Listener para el Like
        holder.ivLike.setOnClickListener { onAction(comment, ActionType.LIKE) }
    }

    override fun getItemCount(): Int = commentList.size
}