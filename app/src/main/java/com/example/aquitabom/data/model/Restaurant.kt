package com.example.aquitabom.data.model

data class Restaurant(
    val id: String?,
    val nome: String,
    val iniciais: String,
    val latitude: String,
    val longitude: String,
    val endereco: String,
    val descricao: String? = null,
    val telefone: String,
    val urlImagem: String? = null,
    val statusLotacao: String? = null
)
