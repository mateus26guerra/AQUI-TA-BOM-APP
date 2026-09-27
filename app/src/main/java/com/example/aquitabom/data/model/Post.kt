package com.example.aquitabom.data.model

data class Post(
    val id: String? = null,
    val titulo: String? = null,
    val descricao: String? = null,
    val imagemUrl: String? = null,
    val likes: Int? = 0,
    val quantidadeComentarios: Int? = 0,
    val nota: Int? = 0,
    val status: String? = null,
    val nomeUsuario: String? = null,
    val nomeRestaurante: String? = null,
    val dataCriacao: String? = null,
    val curtidoPeloUsuario: Boolean? = false
)

data class LikeResponse(
    val postagemId: String,
    val likes: Int,
    val curtidoPeloUsuario: Boolean
)
