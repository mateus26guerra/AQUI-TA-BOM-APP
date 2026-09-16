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
            val result = repository.getFeedPosts()
            result.fold(
                onSuccess = { uiState = FeedUiState.Success(it) },
                onFailure = { uiState = FeedUiState.Error(it.message ?: "Erro ao carregar feed") }
            )
        }
    }
}
