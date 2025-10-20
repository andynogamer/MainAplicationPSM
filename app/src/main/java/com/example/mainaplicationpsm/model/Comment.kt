package com.example.mainaplicationpsm.model

import java.util.Date

data class Comment(
    val id: Int,
    val parentCommentId: Int?,
    val childrenComments : Int?,
    val content: String,
    val createdAt: Date,
    val likes: Int?
)





