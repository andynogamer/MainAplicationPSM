package com.example.mainaplicationpsm.network

import com.example.mainaplicationpsm.model.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.DELETE

interface ApiService {

    // --- AUTENTICACIÓN ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    // --- PUBLICACIONES ---
    // GET /api/posts?page=1&limit=10
    @GET("posts")
    suspend fun getPosts(
        @Header("Authorization") token: String, // <--- AGREGAR ESTO
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PostListResponse>



    // POST /api/posts (Requiere Token)
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String, // Enviaremos el token aquí manualmente por ahora
        @Body request: CreatePostRequest
    ): Response<GenericResponse> // Asegúrate de tener GenericResponse en Responses.kt, o usa PostListResponse si devuelve el post

    @PUT("posts/{id}")
    suspend fun updatePost(
        @Header("Authorization") token: String,
        @Path("id") postId: Int,
        @Body request: UpdatePostRequest
    ): Response<GenericResponse>

    @DELETE("posts/{id}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Path("id") postId: Int
    ): Response<GenericResponse>
    // --- FOROS ---
    @GET("forums")
    suspend fun getForums(): Response<ForumListResponse>

    @POST("forums/join")
    suspend fun joinForum(
        @Header("Authorization") token: String,
        @Body body: Map<String, Int> // { "id_foro": 1 }
    ): Response<GenericResponse>

    // Búsqueda de foros
    @GET("forums/search")
    suspend fun searchForums(
        @Header("Authorization") token: String,
        @Query("q") query: String?,      // Texto a buscar (opcional)
        @Query("order") order: String?   // "newest" o "oldest"
    ): Response<ForumListResponse>

    // Crear un nuevo foro
    @POST("forums")
    suspend fun createForum(
        @Header("Authorization") token: String,
        @Body request: CreateForumRequest
    ): Response<CreateForumResponse>

    @GET("forums/my")
    suspend fun getMyForums(
        @Header("Authorization") token: String
    ):Response<ForumListResponse>

    @GET("posts/forum/{forumId}")
    suspend fun getPostsByForum(
        @Header("Authorization") token: String, // <--- ¡Faltaba esto!
        @Path("forumId") forumId: Int
    ): Response<PostListResponse>

    // --- USUARIOS ---

    // Obtener detalles del usuario (Para llenar los campos al entrar al perfil)
    @GET("users/{id}")
    suspend fun getUserById(
        @Header("Authorization") token: String, // <--- AGREGA ESTO
        @Path("id") userId: Int
    ): Response<User>

    // Actualizar usuario
    @PUT("users/{id}")
    suspend fun updateUser(
        @Header("Authorization") token: String,
        @Path("id") userId: Int,
        @Body request: UpdateUserRequest
    ): Response<GenericResponse>

    // --- COMENTARIOS ---

    @GET("comments/post/{postId}")
    suspend fun getComments(
        @Header("Authorization") token: String,
        @Path("postId") postId: Int
    ): Response<CommentListResponse>

    @POST("comments")
    suspend fun createComment(
        @Header("Authorization") token: String,
        @Body request: CreateCommentRequest
    ): Response<GenericResponse>



    // --- FAVORITOS ---
    @POST("favorites/toggle")
    suspend fun toggleFavorite(
        @Header("Authorization") token: String,
        @Body body: Map<String, Int> // { "postId": 1 }
    ): Response<GenericResponse> // GenericResponse o una respuesta que devuelva { isFavorite: Boolean }

    @GET("favorites")
    suspend fun getFavorites(
        @Header("Authorization") token: String
    ): Response<PostListResponse>

    // --- LIKES ---

    @POST("posts/{id}/like")
    suspend fun togglePostLike(
        @Header("Authorization") token: String,
        @Path("id") postId: Int
    ): Response<GenericResponse> // GenericResponse o Map<String, Any>

    @POST("comments/{commentId}/like")
    suspend fun toggleCommentLike(
        @Header("Authorization") token: String,
        @Path("commentId") commentId: Int
    ): Response<GenericResponse>
}