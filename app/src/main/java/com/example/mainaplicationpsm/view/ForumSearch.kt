package com.example.mainaplicationpsm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.adapter.ForumAdapter
import com.example.mainaplicationpsm.model.ForumProvider

class ForumSearch : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forum_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerMyForums)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val myForums = ForumProvider.forumList.filter { it.isJoined }

        // 1. Pasa la lógica de clic al adaptador
        recyclerView.adapter = ForumAdapter(myForums) { forum ->
            // 2. Esta es la acción que se ejecuta al hacer clic
            val fragment = ForumDetailFragment.newInstance(forum.name, forum.description)

            // 3. Reemplaza el fragmento actual por el de detalle
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null) // Para que el usuario pueda presionar "Atrás"
                .commit()
        }
    }
}