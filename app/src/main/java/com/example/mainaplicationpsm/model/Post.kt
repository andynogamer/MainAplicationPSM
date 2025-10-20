package com.example.mainaplicationpsm.model

import java.util.Date

data class Post(
    val id: Int,
    val title: String,
    val contentText: String?,
    val createdAt: Date,
    val updatedAt: Date?,
    val userName: String,
    val forum: String,
    val images: List<String>?,
    val voteCount: Int?
)












