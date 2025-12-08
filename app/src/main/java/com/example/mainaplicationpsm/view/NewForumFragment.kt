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
import com.example.mainaplicationpsm.ForumDetailFragment
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.CreateForumRequest
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class NewForumFragment : Fragment() {

    private lateinit var ivBanner: ImageView
    private lateinit var etName: EditText
    private lateinit var etDescription: EditText
    private lateinit var btnCreate: Button

    private lateinit var sessionManager: SessionManager
    private var bannerBase64: String? = null

    // Selector de imagen
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                ivBanner.setImageURI(imageUri)
                bannerBase64 = convertUriToBase64(imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_forum, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        ivBanner = view.findViewById(R.id.ivForumBanner)
        etName = view.findViewById(R.id.etForumName)
        etDescription = view.findViewById(R.id.etForumDescription)
        btnCreate = view.findViewById(R.id.btnCreateForum)

        // Abrir galería al tocar el banner
        view.findViewById<FrameLayout>(R.id.flBannerContainer).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnCreate.setOnClickListener {
            createForum()
        }
    }

    private fun createForum() {
        val name = etName.text.toString().trim()
        val desc = etDescription.text.toString().trim()
        val token = sessionManager.fetchAuthToken()

        if (name.isEmpty() || desc.isEmpty()) {
            Toast.makeText(context, "Nombre y descripción son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        if (token == null) {
            Toast.makeText(context, "Sesión no válida, inicia sesión de nuevo", Toast.LENGTH_SHORT).show()
            return
        }

        val request = CreateForumRequest(
            name = name,
            description = desc,
            banner = bannerBase64
        )

        lifecycleScope.launch {
            try {
                // Llamada a la API
                val response = RetrofitClient.apiService.createForum("Bearer $token", request)

                if (response.isSuccessful && response.body() != null) {
                    val forumId = response.body()!!.forumId
                    Toast.makeText(context, "¡Foro creado exitosamente!", Toast.LENGTH_SHORT).show()

                    // Opcional: Ir directamente al detalle del foro creado
                    val detailFragment = ForumDetailFragment.newInstance(
                        forumId, // <--- AÑADIR ID AQUÍ
                        name,
                        desc,
                        bannerBase64,
                        1
                    )

                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container_view, detailFragment)
                        .addToBackStack(null)
                        .commit()

                } else {
                    val error = response.errorBody()?.string()
                    Log.e("API_ERROR", "Error creando foro: $error")
                    Toast.makeText(context, "Error al crear foro", Toast.LENGTH_SHORT).show()
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

            // Redimensionar banner (Un poco más grande que el avatar, ej. 800px ancho)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 800, 450, true)

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
        @JvmStatic
        fun newInstance() = NewForumFragment()
    }
}