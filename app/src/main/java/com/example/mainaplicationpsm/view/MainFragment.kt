package com.example.mainaplicationpsm.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.adapter.PostAdapter
import com.example.mainaplicationpsm.network.RetrofitClient
import kotlinx.coroutines.launch

class MainFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerPost)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        // Llamada a la API para obtener los posts reales
        fetchPosts(recyclerView)
    }

    private fun fetchPosts(recyclerView: RecyclerView?) {
        lifecycleScope.launch {
            try {
                // Petición a la API
                val response = RetrofitClient.apiService.getPosts()

                if (response.isSuccessful) {
                    val posts = response.body()?.posts ?: emptyList()
                    // Asignar el adaptador con la lista real
                    recyclerView?.adapter = PostAdapter(posts)
                } else {
                    Log.e("API", "Error al obtener posts: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Error de conexión: ${e.message}")
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}