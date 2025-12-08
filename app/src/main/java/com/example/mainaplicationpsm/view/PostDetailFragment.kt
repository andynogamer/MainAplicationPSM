package com.example.mainaplicationpsm.view

import android.content.Context
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.adapter.CommentAdapter
import com.example.mainaplicationpsm.model.Comment
import com.example.mainaplicationpsm.model.CreateCommentRequest
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch

class PostDetailFragment : Fragment() {

    private var postId: Int = -1
    private var postTitle: String? = null
    private var postContent: String? = null
    private var postImage: String? = null

    private lateinit var sessionManager: SessionManager
    private lateinit var etComment: EditText
    private lateinit var recyclerComments: RecyclerView
    private lateinit var adapter: CommentAdapter

    // Variable para saber a quién estamos respondiendo (null = comentario nuevo)
    private var replyingToCommentId: Int? = null

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

        // Configurar Post
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

        etComment = view.findViewById(R.id.etCommentInput)
        recyclerComments = view.findViewById(R.id.recyclerComments)
        recyclerComments.layoutManager = LinearLayoutManager(context)

        // Inicializamos adaptador vacío
        adapter = CommentAdapter(emptyList()) { comment, action ->
            when (action) {
                CommentAdapter.ActionType.REPLY -> activateReplyMode(comment)

                // --- NUEVO: Manejar el Like ---
                CommentAdapter.ActionType.LIKE -> toggleCommentLike(comment)
            }
        }
        recyclerComments.adapter = adapter

        view.findViewById<View>(R.id.btnSendComment).setOnClickListener {
            sendComment()
        }

        fetchComments()
    }

    private fun toggleCommentLike(comment: Comment) {
        val token = sessionManager.fetchAuthToken() ?: return

        // 1. Actualización Optimista (Visual)
        // Cambiamos los valores locales antes de que responda el servidor para que se sienta rápido
        if (comment.isLikedByMe) {
            comment.likeCount-- // Restamos 1
        } else {
            comment.likeCount++ // Sumamos 1
        }
        comment.isLikedByMe = !comment.isLikedByMe // Invertimos estado

        // Notificamos al adaptador para que refresque ese item visualmente
        // (notifyDataSetChanged es lo más fácil, aunque notifyItemChanged sería más eficiente)
        adapter.notifyDataSetChanged()

        // 2. Llamada a la API en segundo plano
        lifecycleScope.launch {
            try {
                // Llamamos a la nueva ruta
                val response = RetrofitClient.apiService.toggleCommentLike("Bearer $token", comment.id)

                if (!response.isSuccessful) {
                    // Si falla, revertimos el cambio (Opcional, pero recomendado)
                    // Log.e("LIKE", "Error al dar like: ${response.code()}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun activateReplyMode(comment: Comment) {
        replyingToCommentId = comment.id
        etComment.hint = "Respondiendo a ${comment.username}..."
        etComment.requestFocus()

        // Abrir teclado automáticamente
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etComment, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun fetchComments() {
        val token = sessionManager.fetchAuthToken() ?: return
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getComments("Bearer $token", postId)
                if (response.isSuccessful) {
                    val rawComments = response.body()?.comments ?: emptyList()

                    // Ordenar comentarios: Padres arriba, hijos abajo
                    val organizedList = organizeComments(rawComments)
                    adapter.updateList(organizedList)
                }
            } catch (e: Exception) {
                Log.e("COMMENTS", "Error: ${e.message}")
            }
        }
    }


    private fun organizeComments(rawList: List<Comment>): List<Comment> {
        val result = mutableListOf<Comment>()


        val commentsByParent = rawList.groupBy { it.parentId ?: 0 }


        fun addCommentAndChildren(comment: Comment) {

            result.add(comment)


            val children = commentsByParent[comment.id] ?: emptyList()


            children.forEach { child ->
                addCommentAndChildren(child)
            }
        }


        val rootComments = commentsByParent[0] ?: emptyList()

        rootComments.forEach { root ->
            addCommentAndChildren(root)
        }

        return result
    }

    private fun sendComment() {
        val text = etComment.text.toString().trim()
        if (text.isEmpty()) return

        val token = sessionManager.fetchAuthToken() ?: return

        // Enviamos el parentId si estamos respondiendo, o null si es nuevo
        val request = CreateCommentRequest(postId, text, replyingToCommentId)

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createComment("Bearer $token", request)
                if (response.isSuccessful) {
                    etComment.setText("")
                    etComment.hint = "Escribe un comentario..." // Resetear hint
                    replyingToCommentId = null // Resetear modo respuesta

                    // Ocultar teclado
                    val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(etComment.windowToken, 0)

                    fetchComments() // Recargar para ver el nuevo comentario
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