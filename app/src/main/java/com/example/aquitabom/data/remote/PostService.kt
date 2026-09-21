package com.example.aquitabom.data.remote

import com.example.aquitabom.data.model.LikeResponse
import com.example.aquitabom.data.model.Post
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface PostService {
    @GET("v1/api/postagens")
    suspend fun getPostagens(): Response<List<Post>>

    @GET("v1/api/postagens/minhas")
    suspend fun getMyPostagens(@Header("Authorization") token: String): Response<List<Post>>

    @Multipart
    @POST("v1/api/postagens")
    suspend fun createPost(
        @Header("Authorization") token: String,
        @Query("titulo") titulo: String,
        @Query("descricao") descricao: String?,
        @Query("restauranteId") restauranteId: String,
        @Query("nota") nota: Int,
        @Query("status") status: String?,
        @Part imagem: MultipartBody.Part
    ): Response<Post>

    @POST("v1/api/postagens/{id}/like")
    suspend fun likePost(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<LikeResponse>

    @DELETE("v1/api/postagens/{id}/like")
    suspend fun unlikePost(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<LikeResponse>

    @DELETE("v1/api/postagens/{id}")
    suspend fun deletePost(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<Unit>
}
