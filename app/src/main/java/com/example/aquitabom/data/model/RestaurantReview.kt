package com.example.aquitabom.data.model

data class RestaurantReview(
    val id: String? = null,
    val usuarioId: String? = null,
    val nomeUsuario: String? = null,
    val restauranteId: String? = null,
    val nota: Int = 0,
    val comentario: String? = null,
    val dataCriacao: String? = null
)

data class CreateRestaurantReviewRequest(
    val nota: Int,
    val comentario: String?
)
