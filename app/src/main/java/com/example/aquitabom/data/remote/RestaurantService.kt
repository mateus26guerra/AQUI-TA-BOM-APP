package com.example.aquitabom.data.remote

import com.example.aquitabom.data.model.PaginatedResponse
import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.model.Restaurant
import retrofit2.Response
import retrofit2.http.*

interface RestaurantService {
    @GET("v1/api/restaurantes")
    suspend fun getRestaurantes(@Header("Authorization") token: String): Response<List<Restaurant>>

    @GET("v1/api/restaurantes/{id}/postagens")
    suspend fun getRestaurantPosts(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PaginatedResponse<Post>>
}
