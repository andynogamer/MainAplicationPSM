package com.example.mainaplicationpsm.view

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.RegisterRequest
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class RegisterActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var ivAvatar: ImageView

    // Variable para guardar la foto en texto (Base64)
    private var avatarBase64: String? = null

    // Lanzador para abrir la galería
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = result.data?.data
            if (imageUri != null) {
                // 1. Mostrar la imagen en pantalla
                ivAvatar.setImageURI(imageUri)
                // 2. Convertirla a texto para enviarla
                avatarBase64 = convertUriToBase64(imageUri)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias UI
        ivAvatar = findViewById(R.id.ivAvatar) // La imagen circular
        val etNombre = findViewById<EditText>(R.id.etNombre)
        val etPaterno = findViewById<EditText>(R.id.etApellidoPaterno)
        val etMaterno = findViewById<EditText>(R.id.etApellidoMaterno)
        val etAlias = findViewById<EditText>(R.id.etAlias)
        val etTelefono = findViewById<EditText>(R.id.etTelefono)
        val etCorreo = findViewById<EditText>(R.id.etCorreo)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnRegistrar = findViewById<Button>(R.id.btnRegister)

        // Listener para seleccionar foto
        ivAvatar.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            pickImageLauncher.launch(intent)
        }

        btnRegistrar.setOnClickListener {
            val nombre = etNombre.text.toString().trim()
            val paterno = etPaterno.text.toString().trim()
            val materno = etMaterno.text.toString().trim()
            val alias = etAlias.text.toString().trim()
            val telefono = etTelefono.text.toString().trim()
            val correo = etCorreo.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            // Validaciones
            if (nombre.isEmpty() || paterno.isEmpty() || correo.isEmpty() || pass.isEmpty() || alias.isEmpty() || telefono.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (telefono.length != 10) {
                Toast.makeText(this, "El teléfono debe tener 10 dígitos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear el objeto request (ahora incluye el avatarBase64)
            val registerRequest = RegisterRequest(
                name = nombre,
                lastNameFather = paterno,
                lastNameMother = materno,
                alias = alias,
                phone = telefono,
                email = correo,
                password = pass,
                avatar = avatarBase64 // Puede ser null si no eligió foto
            )

            performRegister(registerRequest)
        }
    }

    private fun performRegister(request: RegisterRequest) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.register(request)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // Guardar sesión (Token e ID)
                    sessionManager.saveAuthToken(loginResponse.token)
                    sessionManager.saveUserId(loginResponse.user.id) // ¡Importante guardar el ID!

                    Toast.makeText(this@RegisterActivity, "¡Bienvenido ${loginResponse.user.firstName}!", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this@RegisterActivity, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Toast.makeText(this@RegisterActivity, "Error: $errorBody", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Fallo conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    // Función auxiliar para convertir Imagen a Base64 (Reutilizada del perfil)
    private fun convertUriToBase64(imageUri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Redimensionar para que no pese tanto (Max 500x500)
            val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 500, 500, true)

            val byteArrayOutputStream = ByteArrayOutputStream()
            // Comprimir a JPEG calidad 80
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
            val byteArray = byteArrayOutputStream.toByteArray()

            // Prefijo necesario para tu backend Node.js
            "data:image/jpeg;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}