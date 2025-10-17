package com.example.mainaplicationpsm.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.adapter.ForumAdapter
import com.example.mainaplicationpsm.model.ForumProvider

class ForumSearch : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Infla el nuevo layout que creamos
        return inflater.inflate(R.layout.fragment_forum_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerMyForums)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Usamos los mismos datos de ejemplo, filtrando los foros a los que ya se unió el usuario
        val myForums = ForumProvider.Companion.forumList.filter { it.isJoined }
        recyclerView.adapter = ForumAdapter(myForums)
    }
}