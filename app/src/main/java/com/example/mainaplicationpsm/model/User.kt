package com.example.mainaplicationpsm.model

import java.util.Date

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNumber: String?,
    val address: String?,
    val profileImageUrl: String?,
    val createdAt: Date
)






