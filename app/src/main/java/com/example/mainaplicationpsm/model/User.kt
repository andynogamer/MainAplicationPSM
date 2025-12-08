package com.example.mainaplicationpsm.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("Id_usuario")
    val id: Int,

    @SerializedName("Nombre")
    val firstName: String,

    @SerializedName("Apellido_paterno")
    val lastNameFather: String,

    @SerializedName("Apellido_materno")
    val lastNameMother: String?, // Puede ser nulo a veces

    @SerializedName("Correo")
    val email: String,

    @SerializedName("Alias")
    val username: String,

    @SerializedName("telefono")
    val phoneNumber: String?,

    // El backend envía la imagen como String base64
    @SerializedName("Avatar")
    val profileImageUrl: String?
)