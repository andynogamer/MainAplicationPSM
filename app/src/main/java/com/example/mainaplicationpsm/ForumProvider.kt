package com.example.mainaplicationpsm

class ForumProvider {
    companion object {
        val forumList = listOf(
            Forum("Foro de Programación", "Discusiones sobre código y desarrollo.", true),
            Forum("Club de Lectura", "Compartiendo libros y autores favoritos.", true),
            Forum("Viajes y Aventuras", "Consejos y experiencias de viaje.", false),
            Forum("Fotografía Digital", "Técnicas y equipos para fotógrafos.", false),
            Forum("Diseño Gráfico", "Herramientas y tendencias de diseño.", true),
            Forum("Cocina Internacional", "Recetas de todo el mundo.", false)
        )
    }
}