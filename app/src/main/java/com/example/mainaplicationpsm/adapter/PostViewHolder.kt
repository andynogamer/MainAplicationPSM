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

    // Referencias a los nuevos IDs del XML
    val user: TextView = view.findViewById(R.id.tvUsuario)
    val foro: TextView = view.findViewById(R.id.tvForo)
    val titulo: TextView = view.findViewById(R.id.tvPostTitle) // Nuevo campo Título
    val descripcion: TextView = view.findViewById(R.id.tvDescripcion)

    // Contenedor de la imagen y la imagen en sí
    val imageContainer: CardView = view.findViewById(R.id.cvImageContainer)
    val foto: ImageView = view.findViewById(R.id.imgPost)

    // Contadores (Opcional, si los usas)
    val likes: TextView = view.findViewById(R.id.tvLikeCount)
    val comments: TextView = view.findViewById(R.id.tvCommentCount)

    fun render(post: Post){
        // 1. Asignar textos
        user.text = post.userName ?: "Anónimo"

        // Si tu modelo Post tiene el ID o nombre del foro, úsalo aquí.
        // Si no, puedes ocultarlo o poner un texto fijo.
        // foro.text = "en Foro General"

        titulo.text = post.title
        descripcion.text = post.contentText

        // Asignar contadores
        likes.text = post.voteCount.toString()
        comments.text = post.commentCount.toString()

        // 2. Lógica de Imagen
        if (!post.postImage.isNullOrEmpty()) {
            // Si hay imagen, hacemos visible el contenedor
            imageContainer.visibility = View.VISIBLE

            try {
                // Decodificar Base64
                val cleanBase64 = post.postImage.substringAfter(",")
                val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)

                Glide.with(foto.context)
                    .asBitmap()
                    .load(decodedString)
                    .into(foto)
            } catch (e: Exception) {
                // Si falla la carga, ocultamos para que no quede un hueco feo
                imageContainer.visibility = View.GONE
            }
        } else {
            // Si NO hay imagen, ocultamos el contenedor (GONE)
            // Al ser ConstraintLayout, los botones subirán automáticamente y se pegarán al texto.
            imageContainer.visibility = View.GONE
        }
    }
}