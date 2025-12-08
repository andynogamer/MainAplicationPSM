package com.example.mainaplicationpsm.model

import com.google.gson.annotations.SerializedName

// Respuesta para Login y Registro
data class LoginResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("token")
    val token: String,

    @SerializedName("user")
    val user: User
)

// Respuesta para obtener Posts (tiene paginación)
data class PostListResponse(
    @SerializedName("posts")
    val posts: List<Post>,

    // Si quieres usar la paginación después, mapea el objeto pagination
    // @SerializedName("pagination") val pagination: Pagination?
)

// Respuesta para obtener Foros
data class ForumListResponse(
    @SerializedName("forums")
    val forums: List<Forum>
)

// Respuesta genérica para mensajes simples (ej. unirse a foro)
data class GenericResponse(
    @SerializedName("message")
    val message: String?,

    @SerializedName("error")
    val error: String?
)


data class CommentListResponse(
    @SerializedName("comments") val comments: List<Comment>
)