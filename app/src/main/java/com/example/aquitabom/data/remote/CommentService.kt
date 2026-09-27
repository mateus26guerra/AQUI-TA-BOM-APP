package com.example.aquitabom.data.remote

import com.example.aquitabom.data.model.Comment
import com.example.aquitabom.data.model.CreateCommentRequest
import retrofit2.Response
import retrofit2.http.*

interface CommentService {
    @GET("v1/api/comentarios/postagem/{postagemId}")
    suspend fun getComments(
        @Header("Authorization") token: String,
        @Path("postagemId") postagemId: String
    ): Response<List<Comment>>

    @POST("v1/api/comentarios/postagem/{postagemId}")
    suspend fun createComment(
        @Header("Authorization") token: String,
        @Path("postagemId") postagemId: String,
        @Body request: CreateCommentRequest
    ): Response<Comment>

    @DELETE("v1/api/comentarios/{comentarioId}")
    suspend fun deleteComment(
        @Header("Authorization") token: String,
        @Path("comentarioId") comentarioId: String
    ): Response<Unit>
}
