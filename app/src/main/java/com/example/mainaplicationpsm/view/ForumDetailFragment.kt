package com.example.mainaplicationpsm

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainaplicationpsm.adapter.PostAdapter
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.view.NewPostFragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import com.example.mainaplicationpsm.utils.SessionManager
import android.widget.EditText
import android.widget.Toast
import com.example.mainaplicationpsm.model.Post
import com.example.mainaplicationpsm.view.PostDetailFragment
import com.example.mainaplicationpsm.model.UpdatePostRequest // Para que no tengas que escribir la ruta completa

class ForumDetailFragment : Fragment() {

    private var forumId: Int = -1
    private var forumName: String? = null
    private var forumDesc: String? = null
    private var forumBanner: String? = null
    private var forumMembers: Int = 0 // NUEVO: Variable para miembros

    private lateinit var adapter: PostAdapter
    private lateinit var sessionManager: SessionManager



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

            forumId = it.getInt(ARG_FORUM_ID, -1)
            forumName = it.getString(ARG_FORUM_NAME)
            forumDesc = it.getString(ARG_FORUM_DESC)
            forumBanner = it.getString(ARG_FORUM_BANNER)
            forumMembers = it.getInt(ARG_FORUM_MEMBERS) // NUEVO: Leer argumento
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_forum_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        val collapsingToolbar = view.findViewById<CollapsingToolbarLayout>(R.id.collapsing_toolbar)
        val ivHeader = view.findViewById<ImageView>(R.id.ivDetailForumBanner)
        val tvDesc = view.findViewById<TextView>(R.id.tvForumDescription)
        val tvMembers = view.findViewById<TextView>(R.id.tvMemberCount) // Referencia al texto de miembros
        val recyclerPosts = view.findViewById<RecyclerView>(R.id.recyclerForumPosts)

        // Configurar datos
        collapsingToolbar.title = forumName
        tvDesc.text = forumDesc
        tvMembers.text = "$forumMembers Miembros" // NUEVO: Mostrar el número real

        // Cargar Banner
        if (!forumBanner.isNullOrEmpty()) {
            try {
                val cleanBase64 = forumBanner!!.substringAfter(",")
                val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
                val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                ivHeader.setImageBitmap(decodedByte)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        } else {
            ivHeader.setImageResource(R.drawable.search_bar_backgorun)
        }

        recyclerPosts.layoutManager = LinearLayoutManager(requireContext())
        fetchForumPosts(recyclerPosts)

        val fabAddPost: FloatingActionButton = view.findViewById(R.id.fabAddPost)
        fabAddPost.setOnClickListener {
            // CORRECCIÓN: Usamos newInstance pasando el ID del foro actual
            val newPostFragment = NewPostFragment.newInstance(forumId)

            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, newPostFragment)
                .addToBackStack(null)
                .commit()
        }
    }

    private fun fetchForumPosts(recyclerView: RecyclerView) {
        if (forumId == -1) return

        val token = sessionManager.fetchAuthToken()
        val currentUserId = sessionManager.fetchUserId() // Obtenemos TU ID

        if (token == null) return

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPostsByForum("Bearer $token", forumId)

                if (response.isSuccessful) {
                    // Convertimos a MutableList para poder borrar elementos después
                    val posts = response.body()?.posts?.toMutableList() ?: mutableListOf()

                    // Inicializamos el adaptador con los nuevos parámetros
                     adapter = PostAdapter(posts, currentUserId) { post, action ->
                        when (action) {
                            PostAdapter.ActionType.DELETE -> {
                                confirmDeletePost(post, token)
                            }

                            // Caso 2: Editar
                            PostAdapter.ActionType.EDIT -> {
                                showEditDialog(post, token)
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
                                // Optimistic update: Cambiamos visualmente primero para que se sienta rápido
                                post.isFavorite = !post.isFavorite
                                adapter.notifyItemChanged(posts.indexOf(post)) // Refrescar solo ese item

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
                    recyclerView.adapter = adapter
                } else {
                    Log.e("API", "Error al cargar posts: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Error de conexión: ${e.message}")
            }
        }
    }

    // --- LÓGICA PARA BORRAR ---
    private fun confirmDeletePost(post: Post, token: String) {
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Eliminar publicación")
            .setMessage("¿Estás seguro? Esta acción no se puede deshacer.")
            .setPositiveButton("Eliminar") { _, _ ->
                performDelete(post, token)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun togglePostLikeApi(post: Post, token: String) {
        lifecycleScope.launch {
            try {
                RetrofitClient.apiService.togglePostLike("Bearer $token", post.id)
            } catch (e: Exception) { e.printStackTrace() }
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
    private fun performDelete(post: Post, token: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deletePost("Bearer $token", post.id)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Publicación eliminada", Toast.LENGTH_SHORT).show()
                    // Actualizar la lista visualmente sin recargar todo
                    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerForumPosts)
                    (recyclerView?.adapter as? PostAdapter)?.removePost(post)
                } else {
                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun showEditDialog(post: Post, token: String) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_post, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etEditTitle)
        val etDesc = dialogView.findViewById<EditText>(R.id.etEditDesc)

        etTitle.setText(post.title)
        etDesc.setText(post.contentText)

        android.app.AlertDialog.Builder(requireContext())
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
        val request = com.example.mainaplicationpsm.model.UpdatePostRequest(newTitle, newDesc)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updatePost("Bearer $token", post.id, request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Publicación actualizada", Toast.LENGTH_SHORT).show()
                    // Aquí lo ideal es recargar la lista para ver los cambios
                    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerForumPosts)
                    fetchForumPosts(recyclerView!!)
                } else {
                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {

        private const val ARG_FORUM_ID = "forum_id"
        private const val ARG_FORUM_NAME = "forum_name"
        private const val ARG_FORUM_DESC = "forum_desc"
        private const val ARG_FORUM_BANNER = "forum_banner"
        private const val ARG_FORUM_MEMBERS = "forum_members" // NUEVO ID

        @JvmStatic
        fun newInstance(id: Int, name: String, description: String, banner: String?, members: Int?) =
            ForumDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FORUM_ID, id) // GUARDAR ID
                    putString(ARG_FORUM_NAME, name)
                    putString(ARG_FORUM_DESC, description)
                    putString(ARG_FORUM_BANNER, banner)
                    putInt(ARG_FORUM_MEMBERS, members ?: 0)// Guardar entero
                }
            }
    }
}