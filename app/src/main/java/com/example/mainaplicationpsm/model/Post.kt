package com.example.mainaplicationpsm.model

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id_publicaciones")
    val id: Int,

    @SerializedName("Id_usuario")
    val userId: Int,

    @SerializedName("Titulo")
    val title: String,

    @SerializedName("Descripcion")
    val contentText: String?,

    // Recibes la fecha como String (ej. "2023-10-27T00:00:00.000Z")
    @SerializedName("Fecha_creacion")
    val createdAt: String,

    // Estos campos vienen del JOIN con la tabla de usuarios en tu query
    @SerializedName("Alias")
    val userName: String?,

    // Estos campos son calculados en el loop del controlador
    @SerializedName("likes")
    val voteCount: Int = 0,

    @SerializedName("commentsCount")
    val commentCount: Int = 0,

    // Campo para saber a qué foro pertenece (si aplica en tu query)
    @SerializedName("id_foro")
    val forumId: Int?
)