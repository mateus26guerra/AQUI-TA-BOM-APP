package com.example.aquitabom.data.model

data class Restaurant(
    val id: Int,
    val nome: String,
    val iniciais: String,
    val categoria: String,
    val endereco: String,
    val nota: String,
    val statusText: String,
    val statusTime: String,
    val lat: Double,
    val lng: Double,
    val fotos: List<String> = emptyList()
)
