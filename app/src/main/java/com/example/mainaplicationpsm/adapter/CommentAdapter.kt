package com.example.mainaplicationpsm.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.Comment

class CommentAdapter(
    private val commentList: List<Comment>
) : RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ImageView = view.findViewById(R.id.ivCommentAvatar)
        val tvUser: TextView = view.findViewById(R.id.tvCommentUser)
        val tvContent: TextView = view.findViewById(R.id.tvCommentText)
        val tvDate: TextView = view.findViewById(R.id.tvCommentDate)
        val tvLikes: TextView = view.findViewById(R.id.tvCommentLikes)
        val ivLike: ImageView = view.findViewById(R.id.ivCommentLike)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CommentViewHolder(layoutInflater.inflate(R.layout.item_comment, parent, false))
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]

        holder.tvUser.text = comment.username
        holder.tvContent.text = comment.content
        holder.tvDate.text = "${comment.date} ${comment.time}" // Muestra fecha y hora
        holder.tvLikes.text = comment.likeCount.toString()

        // Cargar avatar
        if (!comment.userAvatar.isNullOrEmpty()) {
            try {
                val cleanBase64 = comment.userAvatar.substringAfter(",")
                val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                holder.ivAvatar.setImageBitmap(decodedByte)
            } catch (e: Exception) {
                holder.ivAvatar.setImageResource(R.drawable.ic_launcher_background)
            }
        } else {
            holder.ivAvatar.setImageResource(R.drawable.ic_launcher_background)
        }

        // Color del like si ya le diste like (Lógica visual)
        if (comment.isLikedByMe) {
            holder.ivLike.setColorFilter(holder.itemView.context.getColor(R.color.secondary_purple_all_4)) // O color rojo/activo
        } else {
            holder.ivLike.setColorFilter(holder.itemView.context.getColor(android.R.color.darker_gray))
        }
    }

    override fun getItemCount(): Int = commentList.size
}