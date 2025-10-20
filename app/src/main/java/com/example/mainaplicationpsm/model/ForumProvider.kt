package com.example.mainaplicationpsm.model

import java.util.Date

class ForumProvider {
    companion object {
        val forumList = listOf(
            Forum(
                id = 1,
                name = "Android",
                description = "Comunidad para desarrolladores de Android. ¡Preguntas sobre Compose, XML, Kotlin y más!",
                creatorUser = "GoogleFan",
                bannerImageUrl = "https://ejemplo.com/banners/android.png",
                createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 10), // Hace 10 días
                members = 15200,
                isJoined = true // El usuario ya está unido a este
            ),
            Forum(
                id = 2,
                name = "C#",
                description = "Todo sobre .NET, C#, ASP.NET, MAUI y desarrollo backend.",
                creatorUser = "MicroUser",
                bannerImageUrl = "https://ejemplo.com/banners/csharp.jpg",
                createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 30), // Hace 30 días
                members = 8300,
                isJoined = false // El usuario no está unido
            ),
            Forum(
                id = 3,
                name = "SQL Server",
                description = "Consultas, optimización, diseño de bases de datos y todo lo relacionado con SQL Server.",
                creatorUser = "DataMan",
                bannerImageUrl = null, // Sin imagen de banner
                createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 5), // Hace 5 días
                members = 4100,
                isJoined = true // El usuario ya está unido
            ),
            Forum(
                id = 4,
                name = "Angular",
                description = "Discusiones sobre el framework de frontend de Google: componentes, servicios, RxJS.",
                creatorUser = "FrontendHero",
                bannerImageUrl = "https://ejemplo.com/banners/angular.png",
                createdAt = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 2), // Hace 2 días
                members = null, // Número de miembros nulo/desconocido
                isJoined = false
            )
        )
    }
}