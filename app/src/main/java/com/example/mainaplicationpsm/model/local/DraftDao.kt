package com.example.mainaplicationpsm.model.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDraft(draft: Draft)

    @Query("SELECT * FROM drafts_table ORDER BY timestamp DESC")
    suspend fun getAllDrafts(): List<Draft>

    @Delete
    suspend fun deleteDraft(draft: Draft)

    @Query("DELETE FROM drafts_table WHERE id = :id")
    suspend fun deleteDraftById(id: Int)
}