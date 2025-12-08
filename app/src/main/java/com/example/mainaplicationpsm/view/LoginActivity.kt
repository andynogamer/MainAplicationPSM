package com.example.mainaplicationpsm.view

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.mainaplicationpsm.R
import com.example.mainaplicationpsm.model.LoginRequest
import com.example.mainaplicationpsm.network.RetrofitClient
import com.example.mainaplicationpsm.utils.SessionManager
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializar SessionManager
        sessionManager = SessionManager(this)

        // 2. Verificar si ya hay sesión iniciada
        if (sessionManager.fetchAuthToken() != null) {
            goToHome() // Si hay token, saltar login
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        // Configurar padding para las barras del sistema
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.editTextText)
        val etPassword = findViewById<EditText>(R.id.editTextText2)
        val btnLogin = findViewById<Button>(R.id.button)
        val btnRegister = findViewById<Button>(R.id.button3)

        // Acción Botón Login
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val pass = etPassword.text.toString().trim()

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Por favor llena todos los campos", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            loginUser(email, pass)
        }

        // Acción Botón Registrarse (Navegación)
        btnRegister.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(email: String, pass: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, pass))

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!

                    // 3. Guardar el token recibido
                    sessionManager.saveAuthToken(loginResponse.token)
                    sessionManager.saveUserId(loginResponse.user.id)

                    Toast.makeText(this@LoginActivity, "Bienvenido ${loginResponse.user.firstName}", Toast.LENGTH_SHORT).show()
                    goToHome()
                } else {
                    Toast.makeText(this@LoginActivity, "Error: Credenciales inválidas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@LoginActivity, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun goToHome() {
        val intent = Intent(this, MainActivity::class.java)
        // Estas flags limpian la pila para que no puedas volver al login con "atrás"
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}