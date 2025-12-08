package com.example.mainaplicationpsm.model

import com.google.gson.annotations.SerializedName

data class Comment(
    @SerializedName("Id_comentario")
    val id: Int,

    @SerializedName("Id_publicaciones")
    val postId: Int,

    @SerializedName("Comentario")
    val content: String,

    @SerializedName("Fecha")
    val date: String,

    @SerializedName("Hora")
    val time: String,

    @SerializedName("Alias")
    val authorAlias: String
)