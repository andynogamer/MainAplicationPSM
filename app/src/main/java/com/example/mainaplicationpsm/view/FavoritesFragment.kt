package com.example.mainaplicationpsm.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.adapter.PostAdapter
import com.example.mainaplicationpsm.model.Post
import com.example.mainaplicationpsm.model.UpdatePostRequest
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch

class FavoritesFragment : Fragment() {

    private lateinit var sessionManager: SessionManager
    private lateinit var adapter: PostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorites, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Configurar botón atrás
        view.findViewById<View>(R.id.btnBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerFavorites)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        fetchFavorites(recyclerView)
    }

    private fun fetchFavorites(recyclerView: RecyclerView) {
        val token = sessionManager.fetchAuthToken()
        val currentUserId = sessionManager.fetchUserId()

        if (token == null) return

        lifecycleScope.launch {
            try {
                // LLAMADA A LA API DE FAVORITOS
                val response = RetrofitClient.apiService.getFavorites("Bearer $token")

                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.toMutableList() ?: mutableListOf()

                    if (posts.isEmpty()) {
                        Toast.makeText(context, "No tienes favoritos guardados", Toast.LENGTH_SHORT).show()
                    }

                    adapter = PostAdapter(posts, currentUserId) { post, action ->
                        when (action) {
                            PostAdapter.ActionType.DELETE -> confirmDeletePost(post, token)
                            PostAdapter.ActionType.EDIT -> showEditDialog(post, token)

                            PostAdapter.ActionType.OPEN_COMMENTS -> {
                                val fragment = PostDetailFragment.newInstance(
                                    post.id, post.title, post.contentText, post.postImage
                                )
                                parentFragmentManager.beginTransaction()
                                    .replace(R.id.fragment_container_view, fragment)
                                    .addToBackStack(null)
                                    .commit()
                            }
                            PostAdapter.ActionType.TOGGLE_LIKE -> {
                                // 1. Actualización Optimista (Visual instantánea)
                                if (post.isLiked) {
                                    post.voteCount--
                                } else {
                                    post.voteCount++
                                }
                                post.isLiked = !post.isLiked

                                // Notificar cambio visual
                                adapter.notifyItemChanged(posts.indexOf(post))

                                // 2. Llamada a API en segundo plano
                                togglePostLikeApi(post, token)
                            }

                            PostAdapter.ActionType.TOGGLE_FAVORITE -> {

                                removeFavorite(post, token)
                            }
                        }
                    }
                    recyclerView.adapter = adapter
                } else {
                    Log.e("API", "Error al obtener favoritos: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Error de conexión: ${e.message}")
            }
        }
    }

    private fun removeFavorite(post: Post, token: String) {
        // 1. Llamada a la API para quitarlo
        lifecycleScope.launch {
            try {
                val body = mapOf("postId" to post.id)
                RetrofitClient.apiService.toggleFavorite("Bearer $token", body)

                // 2. Eliminarlo visualmente de la lista inmediatamente
                adapter.removePost(post)
                Toast.makeText(context, "Eliminado de favoritos", Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun togglePostLikeApi(post: Post, token: String) {
        lifecycleScope.launch {
            try {
                RetrofitClient.apiService.togglePostLike("Bearer $token", post.id)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    // --- REUTILIZAMOS LA LÓGICA DE BORRAR/EDITAR POR SI ACASO ---
    private fun confirmDeletePost(post: Post, token: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar publicación")
            .setMessage("¿Estás seguro?")
            .setPositiveButton("Eliminar") { _, _ -> performDelete(post, token) }
            .setNegativeButton("Cancelar", null).show()
    }

    private fun performDelete(post: Post, token: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deletePost("Bearer $token", post.id)
                if (response.isSuccessful) adapter.removePost(post)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun showEditDialog(post: Post, token: String) {

        Toast.makeText(context, "Edita desde el inicio", Toast.LENGTH_SHORT).show()
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoritesFragment()
    }
}