package com.example.mainaplicationpsm.model

import com.google.gson.annotations.SerializedName

// Datos para iniciar sesión
data class LoginRequest(
    @SerializedName("correo")
    val email: String,

    @SerializedName("contrasenia") // Nota: Tu backend usa "contrasenia" en authcontroller.js
    val password: String
)

// Datos para registrarse
data class RegisterRequest(
    @SerializedName("nombre") val name: String,
    @SerializedName("apellido_paterno") val lastNameFather: String,
    @SerializedName("apellido_materno") val lastNameMother: String,
    @SerializedName("correo") val email: String,
    @SerializedName("contrasenia") val password: String,
    @SerializedName("alias") val alias: String,
    @SerializedName("telefono") val phone: String,
    @SerializedName("avatar") val avatar: String? = null
)

// Datos para crear un post
data class CreatePostRequest(
    @SerializedName("titulo") val title: String,
    @SerializedName("descripcion") val description: String,
    @SerializedName("id_foro") val forumId: Int? = null,
    @SerializedName("borrador") val isDraft: Boolean = false
)

data class UpdateUserRequest(
    @SerializedName("nombre") val name: String,
    @SerializedName("apellido_paterno") val lastNameFather: String,
    @SerializedName("apellido_materno") val lastNameMother: String,
    @SerializedName("alias") val alias: String,
    @SerializedName("telefono") val phone: String,
    @SerializedName("avatar") val avatar: String? // Base64 string
)

data class CreateForumRequest(
    @SerializedName("nombre") val name: String,
    @SerializedName("descripcion") val description: String,
    @SerializedName("banner") val banner: String? // Base64
)

data class CreateForumResponse(
    @SerializedName("message") val message: String,
    @SerializedName("forumId") val forumId: Int
)