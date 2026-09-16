package com.example.aquitabom.ui.map

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquitabom.data.local.SessionManager
import com.example.aquitabom.data.model.Restaurant
import com.example.aquitabom.data.repository.RestaurantRepository
import com.example.aquitabom.data.repository.RestaurantRepositoryImpl
import kotlinx.coroutines.launch

sealed class MapUiState {
    object Loading : MapUiState()
    data class Success(val restaurants: List<Restaurant>) : MapUiState()
    data class Error(val message: String) : MapUiState()
}

enum class MapViewMode {
    MAP, LIST
}

class MapViewModel(
    application: Application,
    private val repository: RestaurantRepository = RestaurantRepositoryImpl()
) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    
    var uiState by mutableStateOf<MapUiState>(MapUiState.Loading)
        private set

    var viewMode by mutableStateOf(MapViewMode.MAP)
    var searchQuery by mutableStateOf("")
    
    private var allRestaurants: List<Restaurant> = emptyList()

    init {
        loadRestaurants()
    }

    private fun loadRestaurants() {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            uiState = MapUiState.Error("Usuário não autenticado")
            return
        }

        viewModelScope.launch {
            uiState = MapUiState.Loading
            val result = repository.getNearbyRestaurants(token)
            result.fold(
                onSuccess = {
                    allRestaurants = it
                    filterRestaurants()
                },
                onFailure = {
                    uiState = MapUiState.Error(it.message ?: "Erro ao carregar restaurantes")
                }
            )
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        searchQuery = newQuery
        filterRestaurants()
    }

    private fun filterRestaurants() {
        val filtered = if (searchQuery.isBlank()) {
            allRestaurants
        } else {
            allRestaurants.filter {
                it.nome.contains(searchQuery, ignoreCase = true) ||
                it.endereco.contains(searchQuery, ignoreCase = true)
            }
        }
        uiState = MapUiState.Success(filtered)
    }
}
