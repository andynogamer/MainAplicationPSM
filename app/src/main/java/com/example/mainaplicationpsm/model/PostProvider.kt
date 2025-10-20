package com.example.mainaplicationpsm.model

import java.util.Date

class PostProvider {
    companion object{
        val postList = listOf<Post>(
            Post(
                id = 1,
                title = "¡Bienvenido al foro!",
                contentText = "Este es el primer post de ejemplo. Estamos usando datos de prueba (dummy data) para llenar la UI.",
                createdAt = Date(),
                updatedAt = null,
                userName = "Admin",
                forum = "General",
                images = null,
                voteCount = 22
            ),
            Post(
                id = 2,
                title = "Duda sobre Jetpack Compose",
                contentText = "¿Cuál es la mejor forma de manejar el estado en un ViewModel con Compose?",
                createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60), // Hace 1 hora
                updatedAt = Date(),
                userName = "DevNovato",
                forum = "Android",
                images = listOf("https://ejemplo.com/imagen1.png"), // Con una imagen
                voteCount = 5
            ),
            Post(
                id = 3,
                title = "Recomendación de C#",
                contentText = null,
                createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24), // Hace 1 día
                updatedAt = null,
                userName = "BackendDev",
                forum = "C#",
                images = null,
                voteCount = 0
            ),
            Post(
                id = 4,
                title = "Opinión sobre SQL Server",
                contentText = "Me parece una base de datos muy robusta para aplicaciones empresariales.",
                createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 48), // Hace 2 días
                updatedAt = null,
                userName = "DataMan",
                forum = "SQL",
                images = listOf("https://ejemplo.com/img_sql.png", "https://ejemplo.com/img_db.png"), // Con múltiples imágenes
                voteCount = -1
            ),

        )

    }
}