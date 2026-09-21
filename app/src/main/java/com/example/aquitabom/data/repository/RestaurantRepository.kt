package com.example.aquitabom.data.repository

import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.model.Restaurant
import com.example.aquitabom.data.remote.NetworkModule
import com.example.aquitabom.data.remote.RestaurantService

interface RestaurantRepository {
    suspend fun getNearbyRestaurants(token: String): Result<List<Restaurant>>
    suspend fun getRestaurantPosts(token: String, id: String, page: Int, size: Int): Result<List<Post>>
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
}
