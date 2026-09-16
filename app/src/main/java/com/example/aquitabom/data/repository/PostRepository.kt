package com.example.aquitabom.data.repository

import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.remote.NetworkModule
import com.example.aquitabom.data.remote.PostService

interface PostRepository {
    suspend fun getFeedPosts(): Result<List<Post>>
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
}
