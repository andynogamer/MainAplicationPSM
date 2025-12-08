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

    private var forumId: Int = -1

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var ivImagePreview: ImageView
    private lateinit var btnPublish: Button
    private lateinit var sessionManager: SessionManager

    // Variable para guardar la imagen lista para enviar
    private var postImageBase64: String? = null

    // Selector de imagen
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                // 1. Mostrar visualmente
                ivImagePreview.setImageURI(imageUri)
                ivImagePreview.scaleType = ImageView.ScaleType.CENTER_CROP

                // 2. CORRECCIÓN IMPORTANTE: Convertir a Base64 y guardar en la variable
                postImageBase64 = convertUriToBase64(imageUri)

                // Log de depuración para confirmar que se convirtió
                Log.d("DEBUG_IMG", "Imagen convertida. Longitud: ${postImageBase64?.length}")
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
        // Puedes tocar la imagen o el contenedor para abrir la galería
        val cardImage = view.findViewById<View>(R.id.cardImagePreview)
        cardImage.setOnClickListener { openGallery() }
        ivImagePreview.setOnClickListener { openGallery() }

        btnPublish.setOnClickListener {
            createPost()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
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

        // Log antes de enviar para asegurar que la imagen va
        Log.d("DEBUG_POST", "Enviando post con imagen? ${postImageBase64 != null}")

        val request = CreatePostRequest(
            title = title,
            description = desc,
            forumId = forumId,
            isDraft = false,
            image = postImageBase64 // Aquí enviamos la variable que llenamos arriba
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createPost("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "¡Publicación creada!", Toast.LENGTH_SHORT).show()
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

    // --- CORRECCIÓN: Faltaba esta función auxiliar en este archivo ---
    private fun convertUriToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Redimensionar para optimizar (Max 800px)
            // Si la imagen es muy grande (ej. 4MB), el servidor podría rechazarla o tardar mucho
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true)

            val byteArrayOutputStream = ByteArrayOutputStream()
            // Calidad 80% JPEG
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // Prefijo obligatorio para tu backend
            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
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