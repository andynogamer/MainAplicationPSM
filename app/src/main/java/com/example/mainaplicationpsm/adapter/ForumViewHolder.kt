package com.example.mainaplicationpsm.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.Forum
import com.example.mainaplicationpsm.R

class ForumViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val tvForumName: TextView = view.findViewById(R.id.tvForumName)
    val tvForumDescription: TextView = view.findViewById(R.id.tvForumDescription)
    val ivJoinForum: ImageView = view.findViewById(R.id.ivJoinForum)

    fun render(forum: Forum) {
        tvForumName.text = forum.name
        tvForumDescription.text = forum.description
        // Opcional: Cambiar el icono si ya es parte del foro
        // ivJoinForum.setImageResource(if (forum.isJoined) R.drawable.ic_check else R.drawable.ic_person_add)
    }
}