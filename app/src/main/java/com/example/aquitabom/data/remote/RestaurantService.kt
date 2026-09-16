package com.example.aquitabom.data.remote

import com.example.aquitabom.data.model.Restaurant
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface RestaurantService {
    @GET("v1/api/restaurantes")
    suspend fun getRestaurantes(@Header("Authorization") token: String): Response<List<Restaurant>>
}
