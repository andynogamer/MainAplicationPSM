package com.example.mainaplicationpsm.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import com.example.mainaplicationpsm.model.Post
import com.example.mainaplicationpsm.R

class PostViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val user = view.findViewById<TextView>(R.id.tvUsuario)
    val foro = view.findViewById<TextView>(R.id.tvForo)
    val descripcion = view.findViewById<TextView>(R.id.tvDescripcion)
    val foto = view.findViewById<ImageView>(R.id.imgPost)

    fun render(post: Post){
        user.text = post.userName

    }
}