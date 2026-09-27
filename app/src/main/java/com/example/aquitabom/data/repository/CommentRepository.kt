package com.example.aquitabom.data.repository

import com.example.aquitabom.data.model.Comment
import com.example.aquitabom.data.model.CreateCommentRequest
import com.example.aquitabom.data.remote.CommentService
import com.example.aquitabom.data.remote.NetworkModule

interface CommentRepository {
    suspend fun getComments(token: String, postId: String): Result<List<Comment>>
    suspend fun createComment(token: String, postId: String, text: String): Result<Comment>
    suspend fun deleteComment(token: String, commentId: String): Result<Unit>
}

class CommentRepositoryImpl(
    private val service: CommentService = NetworkModule.commentService
) : CommentRepository {
    override suspend fun getComments(token: String, postId: String): Result<List<Comment>> =
        try {
            val response = service.getComments(token, postId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao carregar comentários: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun createComment(token: String, postId: String, text: String): Result<Comment> =
        try {
            val response = service.createComment(token, postId, CreateCommentRequest(text))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao publicar comentário: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteComment(token: String, commentId: String): Result<Unit> =
        try {
            val response = service.deleteComment(token, commentId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao excluir comentário: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
}
