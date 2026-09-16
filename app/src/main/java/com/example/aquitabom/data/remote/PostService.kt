package com.example.aquitabom.data.remote

import com.example.aquitabom.data.model.Post
import retrofit2.Response
import retrofit2.http.GET

interface PostService {
    @GET("v1/api/postagens")
    suspend fun getPostagens(): Response<List<Post>>
}
