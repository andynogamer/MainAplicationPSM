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
                    val adapter = PostAdapter(posts, currentUserId) { post, action ->
                        when (action) {
                            PostAdapter.ActionType.DELETE -> confirmDeletePost(post, token)
                            PostAdapter.ActionType.EDIT -> showEditDialog(post, token)
                            PostAdapter.ActionType.OPEN_DETAIL -> {
                                val fragment = PostDetailFragment.newInstance(post.id, post.title, post.contentText, post.postImage)
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