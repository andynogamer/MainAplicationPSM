package com.example.mainaplicationpsm.model

import com.google.gson.annotations.SerializedName

data class Post(
    @SerializedName("id_publicaciones") val id: Int,
    @SerializedName("Titulo") val title: String,
    @SerializedName("Descripcion") val contentText: String?,
    @SerializedName("Alias") val userName: String?,
    @SerializedName("Fecha_creacion") val createdAt: String,
    @SerializedName("likes") var voteCount: Int = 0,
    @SerializedName("commentsCount") val commentCount: Int = 0,
    @SerializedName("imagen") val postImage: String?,
    @SerializedName("Id_usuario") val userId: Int,

    @SerializedName("is_favorite") var isFavorite: Boolean = false,
    // --- NUEVO CAMPO ---
    @SerializedName("nombre_foro") val forumName: String?,
    @SerializedName("is_liked") var isLiked: Boolean = false // <--- NUEVO
)