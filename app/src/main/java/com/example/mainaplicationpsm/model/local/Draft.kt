package com.example.mainaplicationpsm.model.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "drafts_table")
data class Draft(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val title: String,
    val description: String,
    val forumId: Int,
    val imageBase64: String?,
    val timestamp: Long = System.currentTimeMillis()
)