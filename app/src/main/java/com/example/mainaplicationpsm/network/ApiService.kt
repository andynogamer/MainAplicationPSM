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
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10
    ): Response<PostListResponse>

    // POST /api/posts (Requiere Token)
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") token: String, // Enviaremos el token aquí manualmente por ahora
        @Body request: CreatePostRequest
    ): Response<GenericResponse> // Asegúrate de tener GenericResponse en Responses.kt, o usa PostListResponse si devuelve el post

    // --- FOROS ---
    @GET("forums")
    suspend fun getForums(): Response<ForumListResponse>

    @POST("forums/join")
    suspend fun joinForum(
        @Header("Authorization") token: String,
        @Body body: Map<String, Int> // { "id_foro": 1 }
    ): Response<GenericResponse>

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
}