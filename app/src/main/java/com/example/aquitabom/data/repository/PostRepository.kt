package com.example.aquitabom.data.repository

import com.example.aquitabom.data.model.LikeResponse
import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.remote.NetworkModule
import com.example.aquitabom.data.remote.PostService
import okhttp3.MultipartBody

interface PostRepository {
    suspend fun getFeedPosts(): Result<List<Post>>
    suspend fun getMyPosts(token: String): Result<List<Post>>
    suspend fun createPost(
        token: String,
        titulo: String,
        descricao: String?,
        restauranteId: String,
        nota: Int,
        status: String?,
        imagem: MultipartBody.Part
    ): Result<Post>
    suspend fun likePost(token: String, id: String): Result<LikeResponse>
    suspend fun unlikePost(token: String, id: String): Result<LikeResponse>
    suspend fun deletePost(token: String, id: String): Result<Unit>
}

class PostRepositoryImpl(
    private val service: PostService = NetworkModule.postService
) : PostRepository {
    override suspend fun getFeedPosts(): Result<List<Post>> {
        return try {
            val response = service.getPostagens()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao buscar postagens: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyPosts(token: String): Result<List<Post>> {
        return try {
            val response = service.getMyPostagens("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao buscar suas postagens: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createPost(
        token: String,
        titulo: String,
        descricao: String?,
        restauranteId: String,
        nota: Int,
        status: String?,
        imagem: MultipartBody.Part
    ): Result<Post> {
        return try {
            val response = service.createPost("Bearer $token", titulo, descricao, restauranteId, nota, status, imagem)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao criar postagem: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun likePost(token: String, id: String): Result<LikeResponse> {
        return try {
            val response = service.likePost("Bearer $token", id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao curtir: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun unlikePost(token: String, id: String): Result<LikeResponse> {
        return try {
            val response = service.unlikePost("Bearer $token", id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao descurtir: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deletePost(token: String, id: String): Result<Unit> {
        return try {
            val response = service.deletePost("******", id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao excluir postagem: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
