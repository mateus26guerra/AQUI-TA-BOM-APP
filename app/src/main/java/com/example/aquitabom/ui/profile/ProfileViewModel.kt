package com.example.aquitabom.ui.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquitabom.data.local.SessionManager
import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.repository.PostRepository
import com.example.aquitabom.data.repository.PostRepositoryImpl
import kotlinx.coroutines.launch

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(val posts: List<Post>, val userName: String) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImpl()
    private val sessionManager = SessionManager(application)

    var uiState by mutableStateOf<ProfileUiState>(ProfileUiState.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            uiState = ProfileUiState.Loading
            fetchProfileData()
        }
    }

    fun refreshProfile() {
        viewModelScope.launch {
            isRefreshing = true
            fetchProfileData()
            isRefreshing = false
        }
    }

    fun deletePost(postId: String, onResult: (Result<Unit>) -> Unit) {
        viewModelScope.launch {
            val token = sessionManager.fetchAuthToken()
            if (token == null) {
                onResult(Result.failure(Exception("Usuário não autenticado")))
                return@launch
            }
            val result = repository.deletePost(token, postId)
            if (result.isSuccess) {
                fetchProfileData()
            }
            onResult(result)
        }
    }

    private suspend fun fetchProfileData() {
        val token = sessionManager.fetchAuthToken()
        if (token == null) {
            uiState = ProfileUiState.Error("Usuário não autenticado")
            return
        }

        val result = repository.getMyPosts(token)
        result.fold(
            onSuccess = { posts ->
                val userName = posts.firstOrNull { it.nomeUsuario != null }?.nomeUsuario ?: "Usuário"
                uiState = ProfileUiState.Success(posts, userName)
            },
            onFailure = {
                uiState = ProfileUiState.Error(it.message ?: "Erro ao carregar perfil")
            }
        )
    }
}
