package com.example.mainaplicationpsm.model

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("Id_comentario") val id: Int,
    @SerializedName("Id_padre") val parentId: Int?, // Puede ser nulo
    @SerializedName("Comentario") val content: String,

    // El backend envía fecha y hora separados, los recibimos como String
    @SerializedName("Fecha") val date: String,
    @SerializedName("Hora") val time: String,

    // Datos del Autor
    @SerializedName("Id_usuario") val userId: Int,
    @SerializedName("Alias") val username: String,
    @SerializedName("Avatar") val userAvatar: String?, // Base64

    // Datos de Interacción
    @SerializedName("likes") var likeCount: Int = 0,
    @SerializedName("user_liked") var isLikedByMe: Boolean = false
)