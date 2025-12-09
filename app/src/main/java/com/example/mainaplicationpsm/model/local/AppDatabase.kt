package com.example.mainaplicationpsm.model.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

// 1. CAMBIAR VERSION A 2
@Database(entities = [Draft::class], version = 2)
abstract class AppDatabase : RoomDatabase() {
    abstract fun draftDao(): DraftDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "psm_database"
                )
                    .fallbackToDestructiveMigration() // 2. AGREGAR ESTO (Evita crasheos al actualizar)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}