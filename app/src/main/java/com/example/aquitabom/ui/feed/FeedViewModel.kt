package com.example.aquitabom.ui.feed

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquitabom.data.model.Post
import com.example.aquitabom.data.repository.PostRepository
import com.example.aquitabom.data.repository.PostRepositoryImpl
import kotlinx.coroutines.launch

sealed class FeedUiState {
    object Loading : FeedUiState()
    data class Success(val posts: List<Post>) : FeedUiState()
    data class Error(val message: String) : FeedUiState()
}

class FeedViewModel(
    private val repository: PostRepository = PostRepositoryImpl()
) : ViewModel() {

    var uiState by mutableStateOf<FeedUiState>(FeedUiState.Loading)
        private set

    init {
        loadPosts()
    }

    fun loadPosts() {
        viewModelScope.launch {
            uiState = FeedUiState.Loading
            try {
                val posts = repository.getFeedPosts()
                uiState = FeedUiState.Success(posts)
            } catch (e: Exception) {
                uiState = FeedUiState.Error(e.message ?: "Erro ao carregar feed")
            }
        }
    }
}
