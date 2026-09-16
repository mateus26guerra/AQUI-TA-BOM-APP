package com.example.aquitabom.data.repository

import com.example.aquitabom.data.model.Restaurant
import com.example.aquitabom.data.remote.NetworkModule
import com.example.aquitabom.data.remote.RestaurantService

interface RestaurantRepository {
    suspend fun getNearbyRestaurants(token: String): Result<List<Restaurant>>
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
}
