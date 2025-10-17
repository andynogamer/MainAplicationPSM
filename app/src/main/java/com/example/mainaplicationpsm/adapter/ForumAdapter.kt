package com.example.mainaplicationpsm.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.model.Forum
import com.example.mainaplicationpsm.R

// 1. Añade "onItemClicked" al constructor
class ForumAdapter(
    private val forumList: List<Forum>,
    private val onItemClicked: (Forum) -> Unit
) : RecyclerView.Adapter<ForumViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForumViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return ForumViewHolder(layoutInflater.inflate(R.layout.item_my_forum, parent, false))
    }

    override fun getItemCount(): Int = forumList.size

    override fun onBindViewHolder(holder: ForumViewHolder, position: Int) {
        val forum = forumList[position]
        holder.render(forum)

        // 2. Añade el listener de clic al ítem
        holder.itemView.setOnClickListener { onItemClicked(forum) }
    }
}