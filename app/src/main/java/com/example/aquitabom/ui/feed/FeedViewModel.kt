package com.example.aquitabom.ui.feed

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

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val posts: List<Post>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PostRepository = PostRepositoryImpl()
    private val sessionManager = SessionManager(application)

    var uiState by mutableStateOf<FeedUiState>(FeedUiState.Loading)
        private set

    var isRefreshing by mutableStateOf(false)
        private set

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            uiState = FeedUiState.Loading
            fetchPosts()
        }
    }

    fun refreshPosts() {
        viewModelScope.launch {
            isRefreshing = true
            fetchPosts()
            isRefreshing = false
        }
    }

    private suspend fun fetchPosts() {
        val result = repository.getFeedPosts()
        result.fold(
            onSuccess = { uiState = FeedUiState.Success(it) },
            onFailure = { uiState = FeedUiState.Error(it.message ?: "Erro ao carregar feed") }
        )
    }

    fun toggleLike(post: Post) {
        val token = sessionManager.fetchAuthToken() ?: return
        val postId = post.id ?: return
        val currentlyLiked = post.curtidoPeloUsuario ?: false

        viewModelScope.launch {
            val result = if (currentlyLiked) {
                repository.unlikePost(token, postId)
            } else {
                repository.likePost(token, postId)
            }

            result.onSuccess { response ->
                updatePostInList(postId, response.likes, response.curtidoPeloUsuario)
            }
        }
    }

    private fun updatePostInList(postId: String, newLikes: Int, newLikedStatus: Boolean) {
        val currentState = uiState
        if (currentState is FeedUiState.Success) {
            val updatedPosts = currentState.posts.map {
                if (it.id == postId) {
                    it.copy(likes = newLikes, curtidoPeloUsuario = newLikedStatus)
                } else {
                    it
                }
            }
            uiState = FeedUiState.Success(updatedPosts)
        }
    }
}
