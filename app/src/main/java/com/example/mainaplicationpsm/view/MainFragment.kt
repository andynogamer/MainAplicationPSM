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

class MainFragment : Fragment() {

    private lateinit var adapter: PostAdapter
    private lateinit var sessionManager: SessionManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        val btnFavorites = view.findViewById<View>(R.id.btnGoToFavorites)
        btnFavorites.setOnClickListener {
            // Navegar al fragmento de favoritos
            val fragment = FavoritesFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null) // Para poder volver con el botón "Atrás"
                .commit()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerPost)
        recyclerView?.layoutManager = LinearLayoutManager(requireContext())

        fetchPosts(recyclerView)
    }

    private fun fetchPosts(recyclerView: RecyclerView?) {
        val token = sessionManager.fetchAuthToken()
        val currentUserId = sessionManager.fetchUserId()

        if (token == null) {
            Log.e("API", "No hay token")
            return
        }

        lifecycleScope.launch {
            try {
                // 1. Enviamos el token a getPosts
                val response = RetrofitClient.apiService.getPosts("Bearer $token")

                if (response.isSuccessful) {
                    val posts = response.body()?.posts?.toMutableList() ?: mutableListOf()

                    // 2. Usamos el nuevo constructor del Adaptador
                    adapter = PostAdapter(posts, currentUserId) { post, action ->
                        when (action) {
                            // Caso 1: Borrar
                            PostAdapter.ActionType.DELETE -> {
                            confirmDeletePost(post, token)
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

                            // Caso 2: Editar
                            PostAdapter.ActionType.EDIT -> {
                            showEditDialog(post, token)
                        }
                            PostAdapter.ActionType.TOGGLE_FAVORITE -> {

                                post.isFavorite = !post.isFavorite
                                adapter.notifyItemChanged(posts.indexOf(post))
                                // Llamada a la API en segundo plano
                                toggleFavoriteApi(post, token)
                            }


                            // Caso 3: Abrir Comentarios (Nuevo)
                            PostAdapter.ActionType.OPEN_COMMENTS -> {
                            val fragment = PostDetailFragment.newInstance(
                                post.id,
                                post.title,
                                post.contentText,
                                post.postImage
                            )
                            parentFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container_view, fragment)
                                .addToBackStack(null)
                                .commit()
                        }
                        }
                    }
                    recyclerView?.adapter = adapter
                } else {
                    Log.e("API", "Error al obtener posts: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Error de conexión: ${e.message}")
            }
        }
    }

    private fun toggleFavoriteApi(post: Post, token: String) {
        lifecycleScope.launch {
            try {
                val body = mapOf("postId" to post.id)
                RetrofitClient.apiService.toggleFavorite("Bearer $token", body)
                // Si falla, podrías revertir el cambio visual aquí
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    private fun togglePostLikeApi(post: Post, token: String) {
        // Log antes de enviar
        Log.d("DEBUG_LIKE", "Intentando dar like al Post ID: ${post.id}")

        lifecycleScope.launch {
            try {
                // Hacemos la llamada
                val response = RetrofitClient.apiService.togglePostLike("Bearer $token", post.id)

                // Log después de recibir respuesta
                if (response.isSuccessful) {
                    Log.d("DEBUG_LIKE", " Servidor respondió ÉXITO (Código: ${response.code()})")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("DEBUG_LIKE", " Servidor respondió ERROR: ${response.code()} - $errorBody")
                }
            } catch (e: Exception) {
                Log.e("DEBUG_LIKE", "Error de conexión (App no llegó al servidor): ${e.message}")
                e.printStackTrace()
            }
        }
    }

    // --- LÓGICA DE BORRADO (Igual que en ForumDetail) ---
    private fun confirmDeletePost(post: Post, token: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar publicación")
            .setMessage("¿Estás seguro? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                performDelete(post, token)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performDelete(post: Post, token: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deletePost("Bearer $token", post.id)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Publicación eliminada", Toast.LENGTH_SHORT).show()
                    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerPost)
                    (recyclerView?.adapter as? PostAdapter)?.removePost(post)
                } else {
                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // --- LÓGICA DE EDICIÓN (Igual que en ForumDetail) ---
    private fun showEditDialog(post: Post, token: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_post, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etEditTitle)
        val etDesc = dialogView.findViewById<EditText>(R.id.etEditDesc)

        etTitle.setText(post.title)
        etDesc.setText(post.contentText)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar Publicación")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newTitle = etTitle.text.toString()
                val newDesc = etDesc.text.toString()
                performEdit(post, newTitle, newDesc, token)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performEdit(post: Post, newTitle: String, newDesc: String, token: String) {
        val request = UpdatePostRequest(newTitle, newDesc)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updatePost("Bearer $token", post.id, request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Publicación actualizada", Toast.LENGTH_SHORT).show()
                    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerPost)
                    fetchPosts(recyclerView) // Recargamos la lista
                } else {
                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}