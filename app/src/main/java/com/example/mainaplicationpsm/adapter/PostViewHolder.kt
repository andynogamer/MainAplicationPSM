package com.example.mainaplicationpsm.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mainaplicationpsm.model.Post
import com.example.mainaplicationpsm.R

class PostViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val user: TextView = view.findViewById(R.id.tvUsuario)
    val foro: TextView = view.findViewById(R.id.tvForo)
    val titulo: TextView = view.findViewById(R.id.tvPostTitle)
    val descripcion: TextView = view.findViewById(R.id.tvDescripcion)
    val imageContainer: CardView = view.findViewById(R.id.cvImageContainer)
    val foto: ImageView = view.findViewById(R.id.imgPost)

    // Botones de interacción
    val ivComment: ImageView = view.findViewById(R.id.ivComment)
    val tvCommentCount: TextView = view.findViewById(R.id.tvCommentCount)

    // --- CORRECCIÓN: Agregamos ivFavorite ---
    val ivFavorite: ImageView = view.findViewById(R.id.ivFavorite)

    val ivLike: ImageView = view.findViewById(R.id.ivLike)           // <--- Faltaba esta
    val tvLikeCount: TextView = view.findViewById(R.id.tvLikeCount)
    // ----------------------------------------

    fun render(post: Post){
        user.text = post.userName ?: "Anónimo"

        if (!post.forumName.isNullOrEmpty()) {
            foro.text = "|  ${post.forumName}"
            foro.visibility = View.VISIBLE
        } else {
            foro.visibility = View.GONE
        }

        titulo.text = post.title
        descripcion.text = post.contentText
        tvCommentCount.text = post.commentCount.toString()

        // Lógica de Favorito (Visual)
        if (post.isFavorite) {
            ivFavorite.setImageResource(R.drawable.ic_bookmark) // Asegúrate de tener este icono
        } else {
            ivFavorite.setImageResource(R.drawable.ic_bookmark_border) // Asegúrate de tener este icono
        }

        if (post.isLiked) {
            ivLike.setImageResource(R.drawable.ic_heart_filled)
            // Usamos itemView.context para obtener el color
            ivLike.setColorFilter(itemView.context.getColor(R.color.secondary_purple_all_4))
        } else {
            ivLike.setImageResource(R.drawable.ic_heart_outline)
            // Usamos itemView.context para obtener el color blanco/gris
            ivLike.setColorFilter(itemView.context.getColor(android.R.color.white))
        }

        tvLikeCount.text = post.voteCount.toString()

        // Imagen
        if (!post.postImage.isNullOrEmpty()) {
            imageContainer.visibility = View.VISIBLE
            try {
                val cleanBase64 = post.postImage.substringAfter(",")
                val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
                Glide.with(foto.context).asBitmap().load(decodedString).into(foto)
            } catch (e: Exception) {
                imageContainer.visibility = View.GONE
            }
        } else {
            imageContainer.visibility = View.GONE
        }
    }
}