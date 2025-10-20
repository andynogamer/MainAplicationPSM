package com.example.mainaplicationpsm.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mainaplicationpsm.model.Post
import com.example.mainaplicationpsm.R

class PostViewHolder(view: View): RecyclerView.ViewHolder(view) {

    val user = view.findViewById<TextView>(R.id.tvUsuario)
    val foro = view.findViewById<TextView>(R.id.tvForo)
    val descripcion = view.findViewById<TextView>(R.id.tvDescripcion)
    val foto = view.findViewById<ImageView>(R.id.imgPost)

    fun render(post: Post){
        user.text = post.userName
        foro.text = post.forum
        descripcion.text = post.contentText
        Glide.with(foto.context).load("https://occ-0-8407-2219.1.nflxso.net/dnm/api/v6/6AYY37jfdO6hpXcMjf9Yu5cnmO0/AAAABe67wDJPN2Hd5klgtgARe-jp2vp1q3nA0Q8MAO9-3AuJQxnpVhIdl22ZS6D4uxp5Wst_0SR_JPVKWCt3Wt4wUeBnJyTryMKqElNp.jpg").into(foto)

    }
}