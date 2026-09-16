package com.example.aquitabom.data.model

data class Post(
    val id: String,
    val titulo: String,
    val descricao: String,
    val imagemUrl: String,
    val nomeUsuario: String,
    val nomeRestaurante: String,
    val dataCriacao: String?
)
