package com.example.mainaplicationpsm.view

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.adapter.CommentAdapter
import com.example.mainaplicationpsm.model.CreateCommentRequest
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch

class PostDetailFragment : Fragment() {

    private var postId: Int = -1
    // Variables para recibir datos del post y no tener que pedirlos de nuevo (optimización)
    private var postTitle: String? = null
    private var postContent: String? = null
    private var postImage: String? = null

    private lateinit var sessionManager: SessionManager
    private lateinit var etComment: EditText
    private lateinit var recyclerComments: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            postId = it.getInt(ARG_POST_ID)
            postTitle = it.getString(ARG_POST_TITLE)
            postContent = it.getString(ARG_POST_CONTENT)
            postImage = it.getString(ARG_POST_IMAGE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        // Configurar UI del Post Original
        view.findViewById<TextView>(R.id.tvDetailTitle).text = postTitle
        view.findViewById<TextView>(R.id.tvDetailDesc).text = postContent
        val ivImage = view.findViewById<ImageView>(R.id.ivDetailImage)

        if (!postImage.isNullOrEmpty()) {
            ivImage.visibility = View.VISIBLE
            try {
                val cleanBase64 = postImage!!.substringAfter(",")
                val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
                Glide.with(this).asBitmap().load(decodedString).into(ivImage)
            } catch (e: Exception) { ivImage.visibility = View.GONE }
        }

        // Configurar Lista de Comentarios
        recyclerComments = view.findViewById(R.id.recyclerComments)
        recyclerComments.layoutManager = LinearLayoutManager(context)

        // Configurar Input
        etComment = view.findViewById(R.id.etCommentInput)
        view.findViewById<View>(R.id.btnSendComment).setOnClickListener {
            sendComment()
        }

        // Cargar comentarios
        fetchComments()
    }

    private fun fetchComments() {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getComments("Bearer $token", postId)
                if (response.isSuccessful) {
                    val comments = response.body()?.comments ?: emptyList()
                    recyclerComments.adapter = CommentAdapter(comments)
                }
            } catch (e: Exception) {
                Log.e("COMMENTS", "Error: ${e.message}")
            }
        }
    }

    private fun sendComment() {
        val text = etComment.text.toString().trim()
        if (text.isEmpty()) return

        val token = sessionManager.fetchAuthToken() ?: return
        val request = CreateCommentRequest(postId, text)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createComment("Bearer $token", request)
                if (response.isSuccessful) {
                    etComment.setText("") // Limpiar input
                    fetchComments() // Recargar lista
                    Toast.makeText(context, "Comentario enviado", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al comentar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val ARG_POST_ID = "post_id"
        private const val ARG_POST_TITLE = "post_title"
        private const val ARG_POST_CONTENT = "post_content"
        private const val ARG_POST_IMAGE = "post_image"

        @JvmStatic
        fun newInstance(id: Int, title: String, content: String?, image: String?) =
            PostDetailFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_POST_ID, id)
                    putString(ARG_POST_TITLE, title)
                    putString(ARG_POST_CONTENT, content)
                    putString(ARG_POST_IMAGE, image)
                }
            }
    }
}