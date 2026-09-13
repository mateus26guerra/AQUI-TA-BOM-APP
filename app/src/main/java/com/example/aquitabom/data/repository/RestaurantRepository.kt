package com.example.aquitabom.data.repository

import com.example.aquitabom.data.model.Restaurant

interface RestaurantRepository {
    suspend fun getNearbyRestaurants(): List<Restaurant>
}

class RestaurantRepositoryImpl : RestaurantRepository {
    override suspend fun getNearbyRestaurants(): List<Restaurant> {
        return listOf(
            Restaurant(
                id = 1,
                nome = "Pizzaria da Praça",
                iniciais = "PP",
                categoria = "Pizzaria Artesanal",
                endereco = "Recife Antigo, PE",
                nota = "4.8",
                statusText = "Embaçado",
                statusTime = "~15 min",
                lat = -8.0520,
                lng = -34.8710,
                fotos = listOf("https://images.unsplash.com/photo-1555396273-367ea4eb4db5?w=300")
            ),
            Restaurant(
                id = 2,
                nome = "Rio Branco Burger",
                iniciais = "RB",
                categoria = "Hamburgueria",
                endereco = "Recife Antigo, PE",
                nota = "4.6",
                statusText = "Normal",
                statusTime = "Sem fila",
                lat = -8.0580,
                lng = -34.8725,
                fotos = listOf("https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=300")
            ),
            Restaurant(
                id = 3,
                nome = "Sabor do Bairro",
                iniciais = "SB",
                categoria = "Comida Típica",
                endereco = "Recife Antigo, PE",
                nota = "4.9",
                statusText = "Cheio que só",
                statusTime = "35+ min",
                lat = -8.0620,
                lng = -34.8735,
                fotos = listOf("https://images.unsplash.com/photo-1504674900247-0877df9cc836?w=300")
            )
        )
    }
}
