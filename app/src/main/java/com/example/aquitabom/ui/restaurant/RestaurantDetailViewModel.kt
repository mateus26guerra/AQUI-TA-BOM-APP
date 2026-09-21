package com.example.aquitabom.ui.restaurant

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquitabom.data.local.SessionManager
import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.repository.RestaurantRepository
import com.example.aquitabom.data.repository.RestaurantRepositoryImpl
import kotlinx.coroutines.launch

sealed class RestaurantDetailUiState {
    object Loading : RestaurantDetailUiState()
    data class Success(val posts: List<Post>) : RestaurantDetailUiState()
    data class Error(val message: String) : RestaurantDetailUiState()
}

class RestaurantDetailViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: RestaurantRepository = RestaurantRepositoryImpl()
    private val sessionManager = SessionManager(application)

    var uiState by mutableStateOf<RestaurantDetailUiState>(RestaurantDetailUiState.Loading)
        private set

    fun loadRestaurantPosts(restaurantId: String) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            uiState = RestaurantDetailUiState.Error("Usuário não autenticado")
            return
        }

        viewModelScope.launch {
            uiState = RestaurantDetailUiState.Loading
            val result = repository.getRestaurantPosts(token, restaurantId, 0, 50)
            result.fold(
                onSuccess = { posts ->
                    uiState = RestaurantDetailUiState.Success(posts)
                },
                onFailure = {
                    uiState = RestaurantDetailUiState.Error(it.message ?: "Erro ao carregar postagens")
                }
            )
        }
    }
}
