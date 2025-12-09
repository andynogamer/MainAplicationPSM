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
import com.example.mainaplicationpsm.model.local.AppDatabase
import com.example.mainaplicationpsm.model.local.Draft

class NewPostFragment : Fragment() {

    private var forumId: Int = -1
    private var isDraftMode = false // Bandera: ¿Es un borrador recuperado?
    private var currentDraftId: Int = 0 // ID del borrador en Room

    private lateinit var etTitle: EditText
    private lateinit var etDescription: EditText
    private lateinit var ivImagePreview: ImageView
    private lateinit var btnPublish: Button
    private lateinit var btnSaveDraft: Button // Nuevo botón
    private lateinit var sessionManager: SessionManager

    // Variable para guardar la imagen lista para enviar (Base64 string)
    private var postImageBase64: String? = null

    // Selector de imagen de galería
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                // 1. Mostrar visualmente
                ivImagePreview.setImageURI(imageUri)
                ivImagePreview.scaleType = ImageView.ScaleType.CENTER_CROP

                // 2. Convertir a Base64 y guardar en la variable para enviar/guardar
                postImageBase64 = convertUriToBase64(imageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            forumId = it.getInt(ARG_FORUM_ID, -1)

            // Si recibimos un ID de borrador, activamos el modo edición
            if (it.containsKey(ARG_DRAFT_ID)) {
                isDraftMode = true
                currentDraftId = it.getInt(ARG_DRAFT_ID)
            }
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

        // Vincular vistas
        etTitle = view.findViewById(R.id.etPostTitle)
        etDescription = view.findViewById(R.id.etPostDescription)
        ivImagePreview = view.findViewById(R.id.ivPostImagePreview)
        btnPublish = view.findViewById(R.id.btnPublishPost)
        btnSaveDraft = view.findViewById(R.id.btnSaveDraft) // Asegúrate de tener este ID en el XML

        // --- LÓGICA DE CARGA DE BORRADOR ---
        if (isDraftMode) {
            // Rellenar campos con los datos recibidos
            arguments?.let { args ->
                etTitle.setText(args.getString(ARG_DRAFT_TITLE, ""))
                etDescription.setText(args.getString(ARG_DRAFT_DESC, ""))

                // Cargar imagen si existe
                val savedImage = args.getString(ARG_DRAFT_IMG)
                if (!savedImage.isNullOrEmpty()) {
                    postImageBase64 = savedImage
                    // Mostrar la imagen decodificando el Base64
                    try {
                        val cleanBase64 = savedImage.substringAfter(",")
                        val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
                        val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                        ivImagePreview.setImageBitmap(decodedByte)
                        ivImagePreview.scaleType = ImageView.ScaleType.CENTER_CROP
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            // Cambiar texto del botón para feedback visual
            btnSaveDraft.text = "Actualizar Borrador"
        }
        // ------------------------------------

        // Listeners para selección de imagen
        val cardImage = view.findViewById<View>(R.id.cardImagePreview)
        cardImage.setOnClickListener { openGallery() }
        ivImagePreview.setOnClickListener { openGallery() }

        // Listener: Publicar (API)
        btnPublish.setOnClickListener {
            createPost()
        }

        // Listener: Guardar Borrador (Room)
        btnSaveDraft.setOnClickListener {
            saveDraftLocally()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        pickImageLauncher.launch(intent)
    }

    // --- GUARDAR EN BASE DE DATOS LOCAL (ROOM) ---
    private fun saveDraftLocally() {
        val title = etTitle.text.toString().trim()
        val desc = etDescription.text.toString().trim()

        if (title.isEmpty() && desc.isEmpty()) {
            Toast.makeText(context, "El borrador no puede estar vacío", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            // Creamos el objeto Draft
            // Si isDraftMode es true, usamos currentDraftId para SOBREESCRIBIR el existente.
            // Si es false, usamos 0 para que Room genere un ID nuevo.
            val draft = Draft(
                id = if (isDraftMode) currentDraftId else 0,
                title = title,
                description = desc,
                forumId = forumId,
                imageBase64 = postImageBase64
            )

            // Insertamos (Replace strategy maneja la actualización si el ID ya existe)
            AppDatabase.getDatabase(requireContext()).draftDao().insertDraft(draft)

            Toast.makeText(context, "Borrador guardado localmente", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack() // Salir de la pantalla
        }
    }

    // --- PUBLICAR EN API ---
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
            isDraft = false, // Para la API esto NO es un borrador, es un post real
            image = postImageBase64
        )

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.createPost("Bearer $token", request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "¡Publicación creada!", Toast.LENGTH_SHORT).show()

                    // --- LIMPIEZA IMPORTANTE ---
                    // Si esto era un borrador y ya se publicó con éxito, lo borramos de Room
                    if (isDraftMode) {
                        AppDatabase.getDatabase(requireContext())
                            .draftDao()
                            .deleteDraftById(currentDraftId)
                    }
                    // ---------------------------

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

    private fun convertUriToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Redimensionar para evitar problemas de memoria o payloads gigantes
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 800, true)

            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val ARG_FORUM_ID = "forum_id"

        // Constantes para pasar datos del borrador
        private const val ARG_DRAFT_ID = "draft_id"
        private const val ARG_DRAFT_TITLE = "draft_title"
        private const val ARG_DRAFT_DESC = "draft_desc"
        private const val ARG_DRAFT_IMG = "draft_img"

        // Constructor para NUEVO post
        @JvmStatic
        fun newInstance(forumId: Int) =
            NewPostFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FORUM_ID, forumId)
                }
            }

        // Constructor para ABRIR un BORRADOR
        @JvmStatic
        fun newInstanceFromDraft(draft: Draft) =
            NewPostFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_FORUM_ID, draft.forumId)
                    // Pasamos todos los datos del borrador
                    putInt(ARG_DRAFT_ID, draft.id)
                    putString(ARG_DRAFT_TITLE, draft.title)
                    putString(ARG_DRAFT_DESC, draft.description)
                    putString(ARG_DRAFT_IMG, draft.imageBase64)
                }
            }
    }
}