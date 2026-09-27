package com.example.aquitabom.data.repository

import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.model.Restaurant
import com.example.aquitabom.data.model.LotacaoAvaliacao
import com.example.aquitabom.data.model.CreateRestaurantReviewRequest
import com.example.aquitabom.data.model.RestaurantReview
import com.example.aquitabom.data.remote.NetworkModule
import com.example.aquitabom.data.remote.RestaurantService

interface RestaurantRepository {
    suspend fun getNearbyRestaurants(token: String): Result<List<Restaurant>>
    suspend fun getMapRestaurants(token: String): Result<List<Restaurant>>
    suspend fun avaliarLotacao(token: String, restauranteId: String, status: String): Result<Unit>
    suspend fun getAvaliacaoLotacao(token: String, restauranteId: String): Result<LotacaoAvaliacao>
    suspend fun getRestaurantPosts(token: String, id: String, page: Int, size: Int): Result<List<Post>>
    suspend fun getRestaurantReviews(token: String, restauranteId: String): Result<List<RestaurantReview>>
    suspend fun saveRestaurantReview(token: String, restauranteId: String, nota: Int, comentario: String?): Result<RestaurantReview>
    suspend fun deleteRestaurantReview(token: String, reviewId: String): Result<Unit>
}

class RestaurantRepositoryImpl(
    private val service: RestaurantService = NetworkModule.restaurantService
) : RestaurantRepository {
    override suspend fun getNearbyRestaurants(token: String): Result<List<Restaurant>> {
        return try {
            val response = service.getRestaurantes("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao buscar restaurantes: ${response.code()}"))
            }

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMapRestaurants(token: String): Result<List<Restaurant>> {
        return try {
            val response = service.getRestaurantesMapa(token)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao buscar restaurantes do mapa: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun avaliarLotacao(
        token: String,
        restauranteId: String,
        status: String
    ): Result<Unit> {
        return try {
            val response = service.avaliarLotacao(token, restauranteId, status)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Erro ao avaliar lotação: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAvaliacaoLotacao(
        token: String,
        restauranteId: String
    ): Result<LotacaoAvaliacao> {
        return try {
            val response = service.getAvaliacaoLotacao(token, restauranteId)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Erro ao buscar avaliações de lotação: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRestaurantPosts(token: String, id: String, page: Int, size: Int): Result<List<Post>> {
        return try {
            val response = service.getRestaurantPosts("Bearer $token", id, page, size)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!.content)
            } else {
                Result.failure(Exception("Erro ao buscar postagens do restaurante: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getRestaurantReviews(token: String, restauranteId: String): Result<List<RestaurantReview>> =
        try {
            val response = service.getRestaurantReviews(token, restauranteId)
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("Erro ao carregar avaliações: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun saveRestaurantReview(
        token: String,
        restauranteId: String,
        nota: Int,
        comentario: String?
    ): Result<RestaurantReview> =
        try {
            val response = service.createRestaurantReview(
                token,
                restauranteId,
                CreateRestaurantReviewRequest(nota, comentario)
            )
            if (response.isSuccessful && response.body() != null) Result.success(response.body()!!)
            else Result.failure(Exception("Erro ao salvar avaliação: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }

    override suspend fun deleteRestaurantReview(token: String, reviewId: String): Result<Unit> =
        try {
            val response = service.deleteRestaurantReview(token, reviewId)
            if (response.isSuccessful) Result.success(Unit)
            else Result.failure(Exception("Erro ao excluir avaliação: ${response.code()}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
}
