package com.example.aquitabom.data.model

data class Comment(
    val id: String? = null,
    val usuarioId: String? = null,
    val nomeUsuario: String? = null,
    val postagemId: String? = null,
    val texto: String,
    val dataCriacao: String? = null
)

data class CreateCommentRequest(
    val texto: String
)
