package com.example.aquitabom.ui.restaurant

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquitabom.data.local.SessionManager
import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.model.LotacaoAvaliacao
import com.example.aquitabom.data.model.RestaurantReview
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
    var isSendingLotacao by mutableStateOf(false)
        private set
    var lotacaoError by mutableStateOf<String?>(null)
        private set
    var lotacaoAvaliacao by mutableStateOf<LotacaoAvaliacao?>(null)
        private set
    var restaurantReviews by mutableStateOf<List<RestaurantReview>>(emptyList())
        private set
    var isLoadingReviews by mutableStateOf(false)
        private set
    var isSendingReview by mutableStateOf(false)
        private set
    var reviewError by mutableStateOf<String?>(null)
        private set
    var isRefreshing by mutableStateOf(false)
        private set

    fun loadRestaurantPosts(restaurantId: String) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            uiState = RestaurantDetailUiState.Error("Usuário não autenticado")
            return
        }

        viewModelScope.launch {
            fetchRestaurantData(token, restaurantId, showLoading = true)
        }
    }

    fun refreshRestaurant(restaurantId: String) {
        viewModelScope.launch {
            isRefreshing = true
            val token = sessionManager.fetchAuthToken()
            if (token == null) {
                uiState = RestaurantDetailUiState.Error("Usuário não autenticado")
            } else {
                fetchRestaurantData(token, restaurantId, showLoading = false)
            }
            isRefreshing = false
        }
    }

    private suspend fun fetchRestaurantData(
        token: String,
        restaurantId: String,
        showLoading: Boolean
    ) {
        if (showLoading) {
            uiState = RestaurantDetailUiState.Loading
        }
        val result = repository.getRestaurantPosts(token, restaurantId, 0, 50)
        result.fold(
            onSuccess = { posts -> uiState = RestaurantDetailUiState.Success(posts) },
            onFailure = {
                uiState = RestaurantDetailUiState.Error(it.message ?: "Erro ao carregar postagens")
            }
        )
        repository.getAvaliacaoLotacao(token, restaurantId).onSuccess {
            lotacaoAvaliacao = it
        }
        fetchRestaurantReviews(token, restaurantId)
    }

    fun loadRestaurantReviews(restaurantId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        viewModelScope.launch {
            fetchRestaurantReviews(token, restaurantId)
        }
    }

    private suspend fun fetchRestaurantReviews(token: String, restaurantId: String) {
        isLoadingReviews = true
        repository.getRestaurantReviews(token, restaurantId).fold(
            onSuccess = { restaurantReviews = it },
            onFailure = { reviewError = it.message ?: "Erro ao carregar avaliações" }
        )
        isLoadingReviews = false
    }

    fun saveRestaurantReview(
        restaurantId: String,
        nota: Int,
        comentario: String?,
        onSuccess: () -> Unit
    ) {
        val token = sessionManager.fetchAuthToken() ?: return
        viewModelScope.launch {
            isSendingReview = true
            reviewError = null
            repository.saveRestaurantReview(token, restaurantId, nota, comentario).fold(
                onSuccess = {
                    fetchRestaurantReviews(token, restaurantId)
                    isSendingReview = false
                    onSuccess()
                },
                onFailure = {
                    isSendingReview = false
                    reviewError = it.message ?: "Erro ao salvar avaliação"
                }
            )
        }
    }

    fun deleteRestaurantReview(reviewId: String, restaurantId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        viewModelScope.launch {
            repository.deleteRestaurantReview(token, reviewId).fold(
                onSuccess = { loadRestaurantReviews(restaurantId) },
                onFailure = { reviewError = it.message ?: "Erro ao excluir avaliação" }
            )
        }
    }

    fun avaliarLotacao(restauranteId: String, status: String, onSuccess: () -> Unit) {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            lotacaoError = "Usuário não autenticado"
            return
        }
        viewModelScope.launch {
            isSendingLotacao = true
            lotacaoError = null
            repository.avaliarLotacao(token, restauranteId, status).fold(
                onSuccess = {
                    isSendingLotacao = false
                    onSuccess()
                },
                onFailure = {
                    isSendingLotacao = false
                    lotacaoError = it.message ?: "Não foi possível enviar a avaliação"
                }
            )
        }
    }

    fun loadLotacao(restauranteId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        viewModelScope.launch {
            repository.getAvaliacaoLotacao(token, restauranteId).onSuccess {
                lotacaoAvaliacao = it
            }
        }
    }
}
