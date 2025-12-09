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
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var sessionManager: SessionManager

    // Variables de Paginación
    private var currentPage = 1
    private var isLoading = false
    private var isLastPage = false
    private val PAGE_SIZE = 10

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())
        val currentUserId = sessionManager.fetchUserId()

        val btnFavorites = view.findViewById<View>(R.id.btnGoToFavorites)
        btnFavorites.setOnClickListener {
            val fragment = FavoritesFragment.newInstance()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, fragment)
                .addToBackStack(null)
                .commit()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerPost)
        layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.layoutManager = layoutManager


        adapter = PostAdapter(mutableListOf(), currentUserId) { post, action ->
            when (action) {
                PostAdapter.ActionType.DELETE -> confirmDeletePost(post)
                PostAdapter.ActionType.EDIT -> showEditDialog(post)

                PostAdapter.ActionType.OPEN_COMMENTS -> {
                    val fragment = PostDetailFragment.newInstance(
                        post.id, post.title, post.contentText, post.postImage
                    )
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                PostAdapter.ActionType.TOGGLE_FAVORITE -> {
                    post.isFavorite = !post.isFavorite
                    val index = adapter.getPostIndex(post) // Si tienes este método helper, o usa indexOf
                    if (index != -1) adapter.notifyItemChanged(index)
                    toggleFavoriteApi(post)
                }

                PostAdapter.ActionType.TOGGLE_LIKE -> {
                    if (post.isLiked) post.voteCount-- else post.voteCount++
                    post.isLiked = !post.isLiked


                    adapter.notifyDataSetChanged()

                    togglePostLikeApi(post)
                }
            }
        }
        recyclerView?.adapter = adapter


        recyclerView?.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                        && firstVisibleItemPosition >= 0
                        && totalItemCount >= PAGE_SIZE
                    ) {
                        loadPosts()
                    }
                }
            }
        })


        loadPosts()
    }

    private fun loadPosts() {
        val token = sessionManager.fetchAuthToken() ?: return
        isLoading = true

        lifecycleScope.launch {
            try {

                val response = RetrofitClient.apiService.getPosts("Bearer $token", currentPage, PAGE_SIZE)

                if (response.isSuccessful) {
                    val newPosts = response.body()?.posts ?: emptyList()

                    if (newPosts.isNotEmpty()) {

                        adapter.addPosts(newPosts)


                        currentPage++
                    } else {

                        isLastPage = true
                    }
                } else {
                    Log.e("API", "Error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("API", "Error conexión: ${e.message}")
            } finally {
                isLoading = false
            }
        }
    }



    private fun toggleFavoriteApi(post: Post) {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            try {
                val body = mapOf("postId" to post.id)
                RetrofitClient.apiService.toggleFavorite("Bearer $token", body)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun togglePostLikeApi(post: Post) {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            try {
                RetrofitClient.apiService.togglePostLike("Bearer $token", post.id)
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun confirmDeletePost(post: Post) {
        AlertDialog.Builder(requireContext())
            .setTitle("Eliminar publicación")
            .setMessage("¿Estás seguro?")
            .setPositiveButton("Eliminar") { _, _ -> performDelete(post) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performDelete(post: Post) {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.deletePost("Bearer $token", post.id)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Eliminado", Toast.LENGTH_SHORT).show()
                    adapter.removePost(post)
                } else {
                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(post: Post) {
        val token = sessionManager.fetchAuthToken() ?: return
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_post, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etEditTitle)
        val etDesc = dialogView.findViewById<EditText>(R.id.etEditDesc)

        etTitle.setText(post.title)
        etDesc.setText(post.contentText)

        AlertDialog.Builder(requireContext())
            .setTitle("Editar")
            .setView(dialogView)
            .setPositiveButton("Guardar") { _, _ ->
                val newTitle = etTitle.text.toString()
                val newDesc = etDesc.text.toString()
                performEdit(post, newTitle, newDesc, token)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun performEdit(post: Post, title: String, desc: String, token: String) {
        val request = UpdatePostRequest(title, desc)
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.updatePost("Bearer $token", post.id, request)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Actualizado", Toast.LENGTH_SHORT).show()

                    adapter.clear()
                    currentPage = 1
                    isLastPage = false
                    loadPosts()
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