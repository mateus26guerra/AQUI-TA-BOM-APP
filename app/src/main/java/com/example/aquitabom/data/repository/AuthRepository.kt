package com.example.aquitabom.data.repository

import com.example.aquitabom.data.model.LoginRequest
import com.example.aquitabom.data.model.LoginResponse
import com.example.aquitabom.data.model.RegisterRequest
import com.example.aquitabom.data.model.RegisterResponse
import com.example.aquitabom.data.remote.AuthService
import com.example.aquitabom.data.remote.NetworkModule

interface AuthRepository {
    suspend fun login(request: LoginRequest): Result<LoginResponse>
    suspend fun register(request: RegisterRequest): Result<RegisterResponse>
}

class AuthRepositoryImpl(
    private val authService: AuthService = NetworkModule.authService
) : AuthRepository {
    override suspend fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val response = authService.login(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when(response.code()) {
                    401 -> "E-mail ou senha incorretos"
                    else -> "Erro ao entrar: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(request: RegisterRequest): Result<RegisterResponse> {
        return try {
            val response = authService.register(request)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                val errorMsg = when(response.code()) {
                    409 -> "Este e-mail já está cadastrado"
                    else -> "Erro no cadastro: ${response.code()}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
