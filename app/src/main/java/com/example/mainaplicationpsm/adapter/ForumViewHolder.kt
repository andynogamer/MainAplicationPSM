package com.example.mainaplicationpsm.adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.model.Forum
import com.example.mainaplicationpsm.R

class ForumViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    // 1. Usamos los IDs NUEVOS definidos en item_my_forum.xml
    val ivBanner: ImageView = view.findViewById(R.id.ivItemForumBanner)
    val tvName: TextView = view.findViewById(R.id.tvItemForumName)
    val tvDesc: TextView = view.findViewById(R.id.tvItemForumDesc)

    fun render(forum: Forum) {
        tvName.text = forum.name
        tvDesc.text = forum.description

        // 2. Lógica para mostrar el banner (Imagen)
        if (!forum.bannerImageUrl.isNullOrEmpty()) {
            try {
                // Limpiamos el prefijo "data:image..."
                val cleanBase64 = forum.bannerImageUrl.substringAfter(",")
                // Decodificamos el texto a bytes
                val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
                // Convertimos bytes a imagen (Bitmap)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)

                ivBanner.setImageBitmap(decodedByte)
            } catch (e: Exception) {
                e.printStackTrace()
                // Si falla, ponemos un fondo gris o imagen por defecto
                ivBanner.setImageResource(R.drawable.search_bar_backgorun)
            }
        } else {
            // Si no hay banner, imagen por defecto
            ivBanner.setImageResource(R.drawable.search_bar_backgorun)
        }
    }
}