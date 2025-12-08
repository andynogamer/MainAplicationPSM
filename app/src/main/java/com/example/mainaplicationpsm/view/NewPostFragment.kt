package com.example.mainaplicationpsm.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.CreatePostRequest
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class NewPostFragment : Fragment() {

    private var forumId: Int = -1 // ID del foro al que pertenece el post

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var ivImagePreview: ImageView
    private lateinit var btnPublish: Button
    private lateinit var sessionManager: SessionManager

    // Variable para la imagen en Base64 (Opcional en posts, pero bueno tenerla)
    // Nota: Tu backend actual de posts no parece guardar imagen en la tabla 'publicaciones'
    // pero lo dejaremos listo por si implementas la tabla multimedia después.
    private var postImageBase64: String? = null

    // Selector de imagen
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                ivImagePreview.setImageURI(imageUri)
                // ivImagePreview.scaleType = ImageView.ScaleType.CENTER_CROP
                // Aquí podrías convertir a Base64 si tu API lo soportara en el futuro
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            forumId = it.getInt(ARG_FORUM_ID, -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        etTitle = view.findViewById(R.id.etPostTitle)
        etDescription = view.findViewById(R.id.etPostDescription)
        ivImagePreview = view.findViewById(R.id.ivPostImagePreview)
        btnPublish = view.findViewById(R.id.btnPublishPost)

        // Configurar selección de imagen
        ivImagePreview.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnPublish.setOnClickListener {
            createPost()
        }
    }

    private fun createPost() {
        val title = etTitle.text.toString().trim()
        val desc = etDescription.text.toString().trim()
        val token = sessionManager.fetchAuthToken()

        if (title.isEmpty() || desc.isEmpty()) {
            Toast.makeText(context, "Título y descripción son requeridos", Toast.LENGTH_SHORT).show()
            return
        }

        if (forumId == -1) {
            Toast.makeText(context, "Error: No se identificó el foro", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreatePostRequest(
            title = title,
            description = desc,
            forumId = forumId,
            isDraft = false
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createPost("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "¡Publicación creada!", Toast.LENGTH_SHORT).show()
                    // Volver al foro
                    parentFragmentManager.popBackStack()
                } else {
                    val error = response.errorBody()?.string()
                    Log.e("API_POST", "Error: $error")
                    Toast.makeText(context, "Error al publicar: $error", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    companion object {
        private const val ARG_FORUM_ID = "forum_id"

        @JvmStatic
        fun newInstance(forumId: Int) =
            NewPostFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FORUM_ID, forumId)
                }
            }
    }
}