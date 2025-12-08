package com.example.mainaplicationpsm.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.ForumDetailFragment
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.adapter.ForumAdapter
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch

class ForumSearch : Fragment() {

    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forum_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerMyForums)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Cargar MIS foros por defecto
        fetchMyForums(recyclerView)
    }

    private fun fetchMyForums(recyclerView: RecyclerView) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) return

        lifecycleScope.launch {
            try {
                // Llamamos a getMyForums en lugar de getForums
                val response = RetrofitClient.apiService.getMyForums("Bearer $token")

                if (response.isSuccessful) {
                    val forums = response.body()?.forums ?: emptyList()

                    if (forums.isEmpty()) {
                        Toast.makeText(context, "Aún no te has unido a ningún foro", Toast.LENGTH_SHORT).show()
                    }

                    // Configuramos el adaptador
                    recyclerView.adapter = ForumAdapter(forums) { forum ->
                        // --- ACCIÓN AL HACER CLIC ---
                        // Abrimos el detalle del foro seleccionado
                        val fragment = ForumDetailFragment.newInstance(
                            forum.id, // <--- AÑADIR ID AQUÍ AL PRINCIPIO
                            forum.name,
                            forum.description,
                            forum.bannerImageUrl,
                            forum.members
                        )

                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragment_container_view, fragment)
                            .addToBackStack(null) // Permite volver atrás con el botón del celular
                            .commit()
                    }
                } else {
                    Log.e("API", "Error al cargar mis foros: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Error conexión: ${e.message}")
            }
        }
    }
}