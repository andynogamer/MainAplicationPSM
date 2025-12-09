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
import com.bumptech.glide.Glide
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.UpdateUserRequest
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private lateinit var sessionManager: SessionManager

    // UI Elements
    private lateinit var ivAvatar: ImageView
    private lateinit var etName: EditText
    private lateinit var etPaterno: EditText
    private lateinit var etMaterno: EditText
    private lateinit var etAlias: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button

    private lateinit var btnMyDrafts: Button


    // Variable para guardar el string base64 de la nueva imagen
    private var newAvatarBase64: String? = null

    // Lanzador para abrir la galería
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                // 1. Mostrar la imagen seleccionada inmediatamente
                ivAvatar.setImageURI(imageUri)

                // 2. Convertir a Base64 para enviar a la API
                newAvatarBase64 = convertUriToBase64(imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sessionManager = SessionManager(requireContext())

        initViews(view)
        loadUserData()
        setupListeners()
    }

    private fun initViews(view: View) {
        ivAvatar = view.findViewById(R.id.ivProfileAvatar)
        etName = view.findViewById(R.id.etProfileName)
        etPaterno = view.findViewById(R.id.etProfileLastNameFather)
        etMaterno = view.findViewById(R.id.etProfileLastNameMother)
        etAlias = view.findViewById(R.id.etProfileAlias)
        etPhone = view.findViewById(R.id.etProfilePhone)
        etEmail = view.findViewById(R.id.etProfileEmail)
        btnSave = view.findViewById(R.id.btnSaveChanges)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnMyDrafts = view.findViewById(R.id.btnMyDrafts)
    }

    private fun setupListeners() {
        // Clic en la foto para cambiarla
        ivAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnMyDrafts.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_view, DraftsFragment())
                .addToBackStack(null)
                .commit()
        }

        // Guardar cambios
        btnSave.setOnClickListener {
            saveChanges()
        }

        // Logout
        btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }

    private fun loadUserData() {
        // 1. Obtener ID y TOKEN
        val userId = sessionManager.fetchUserId()
        val token = sessionManager.fetchAuthToken() // <--- Recuperamos el token

        if (userId == -1 || token == null) return // <--- Validamos ambos

        lifecycleScope.launch {
            try {
                // 2. Pedir los datos enviando el token
                // Agregamos "Bearer " antes del token
                val response = RetrofitClient.apiService.getUserById("Bearer $token", userId)

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!

                    // 3. Llenar los campos
                    etName.setText(user.firstName)
                    etPaterno.setText(user.lastNameFather)
                    etMaterno.setText(user.lastNameMother)
                    etAlias.setText(user.username)
                    etPhone.setText(user.phoneNumber)
                    etEmail.setText(user.email)

                    // 4. Cargar imagen
                    if (!user.profileImageUrl.isNullOrEmpty()) {
                        try {
                            val cleanBase64 = user.profileImageUrl.substringAfter(",")
                            val decodedString = Base64.decode(cleanBase64, Base64.DEFAULT)
                            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                            ivAvatar.setImageBitmap(decodedByte)
                        } catch (e: Exception) {
                            Log.e("PROFILE", "Error imagen: ${e.message}")
                            Glide.with(requireContext())
                                .load(user.profileImageUrl)
                                .placeholder(R.drawable.ic_launcher_background)
                                .into(ivAvatar)
                        }
                    }
                } else {
                    // Tip: Agrega esto para saber exactamente qué error da el servidor en el Logcat
                    Log.e("PROFILE_ERROR", "Error: ${response.code()} ${response.errorBody()?.string()}")
                    Toast.makeText(context, "No se pudo cargar la información", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun saveChanges() {
        val userId = sessionManager.fetchUserId()
        val token = sessionManager.fetchAuthToken()

        if (userId == -1 || token == null) return

        val request = UpdateUserRequest(
            name = etName.text.toString(),
            lastNameFather = etPaterno.text.toString(),
            lastNameMother = etMaterno.text.toString(),
            alias = etAlias.text.toString(),
            phone = etPhone.text.toString(),
            avatar = newAvatarBase64
        )

        lifecycleScope.launch {
            try {

                val response = RetrofitClient.apiService.updateUser("Bearer $token", userId, request)

                if (response.isSuccessful) {
                    Toast.makeText(context, "Perfil actualizado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    // Tip: Imprime el error en el Logcat para ver más detalles si falla
                    val error = response.errorBody()?.string()
                    Log.e("API_ERROR", "Error al actualizar: $error")
                    Toast.makeText(context, "Error: $error", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Función auxiliar para convertir Imagen de Galería a Base64
    private fun convertUriToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Redimensionar si es muy grande (opcional pero recomendado para no saturar el servidor)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true)

            val byteArrayOutputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // El backend espera el prefijo data:image... según tu authController
            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = ProfileFragment()
    }
}