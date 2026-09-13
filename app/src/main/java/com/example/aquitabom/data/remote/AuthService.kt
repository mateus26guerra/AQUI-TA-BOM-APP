package com.example.aquitabom.data.remote

import com.example.aquitabom.data.model.LoginRequest
import com.example.aquitabom.data.model.LoginResponse
import com.example.aquitabom.data.model.RegisterRequest
import com.example.aquitabom.data.model.RegisterResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthService {
    @POST("v1/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("v1/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
