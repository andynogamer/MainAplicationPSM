package com.example.mainaplicationpsm.model

import java.util.Date

data class Forum(
    val id: Int,
    val name: String,
    val description: String,
    val creatorUser: String,
    val bannerImageUrl: String?,
    val createdAt: Date,
    val members: Int?,
    var isJoined: Boolean = false
)





