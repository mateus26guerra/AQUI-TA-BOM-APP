package com.example.aquitabom.data.remote

import com.example.aquitabom.data.model.PaginatedResponse
import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.model.Restaurant
import com.example.aquitabom.data.model.LotacaoAvaliacao
import com.example.aquitabom.data.model.CreateRestaurantReviewRequest
import com.example.aquitabom.data.model.RestaurantReview
import retrofit2.Response
import retrofit2.http.*

interface RestaurantService {
    @GET("v1/api/restaurantes")
    suspend fun getRestaurantes(@Header("Authorization") token: String): Response<List<Restaurant>>

    @GET("v1/api/restaurantes/mapa")
    suspend fun getRestaurantesMapa(
        @Header("Authorization") token: String
    ): Response<List<Restaurant>>

    @POST("v1/api/avaliacoes-lotacao/restaurante/{restauranteId}")
    suspend fun avaliarLotacao(
        @Header("Authorization") token: String,
        @Path("restauranteId") restauranteId: String,
        @Query("status") status: String
    ): Response<Unit>

    @GET("v1/api/avaliacoes-lotacao/restaurante/{restauranteId}")
    suspend fun getAvaliacaoLotacao(
        @Header("Authorization") token: String,
        @Path("restauranteId") restauranteId: String
    ): Response<LotacaoAvaliacao>

    @GET("v1/api/avaliacoes-restaurantes/restaurante/{restauranteId}")
    suspend fun getRestaurantReviews(
        @Header("Authorization") token: String,
        @Path("restauranteId") restauranteId: String
    ): Response<List<RestaurantReview>>

    @POST("v1/api/avaliacoes-restaurantes/restaurante/{restauranteId}")
    suspend fun createRestaurantReview(
        @Header("Authorization") token: String,
        @Path("restauranteId") restauranteId: String,
        @Body request: CreateRestaurantReviewRequest
    ): Response<RestaurantReview>

    @DELETE("v1/api/avaliacoes-restaurantes/{avaliacaoId}")
    suspend fun deleteRestaurantReview(
        @Header("Authorization") token: String,
        @Path("avaliacaoId") avaliacaoId: String
    ): Response<Unit>

    @GET("v1/api/restaurantes/{id}/postagens")
    suspend fun getRestaurantPosts(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<PaginatedResponse<Post>>
}
