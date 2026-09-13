package com.example.aquitabom.data.model

data class RegisterRequest(
    val nome: String,
    val email: String,
    val senha: String
)

data class RegisterResponse(
    val id: String,
    val nome: String,
    val email: String,
    val roles: List<String>,
    val authorities: List<String>
)

data class LoginRequest(
    val email: String,
    val senha: String
)

data class LoginResponse(
    val accessToken: String,
    val tokenType: String,
    val expiresIn: Int
)
