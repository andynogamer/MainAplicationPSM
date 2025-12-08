package com.example.mainaplicationpsm.model

import com.google.gson.annotations.SerializedName

data class Forum(
    @SerializedName("id_foro")
    val id: Int,

    @SerializedName("nombre")
    val name: String,

    @SerializedName("descripcion")
    val description: String,

    // El backend no estaba enviando "creatorUser" en la query, lo hacemos opcional o lo quitamos
    // @SerializedName("creador") val creatorUser: String?,

    @SerializedName("banner")
    val bannerImageUrl: String?,

    @SerializedName("fecha_creacion")
    val createdAt: String, // String para evitar errores de parseo

    @SerializedName("total_miembros")
    val members: Int?
)