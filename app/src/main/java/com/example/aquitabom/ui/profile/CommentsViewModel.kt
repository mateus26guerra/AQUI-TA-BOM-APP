package com.example.aquitabom.ui.profile

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquitabom.data.local.SessionManager
import com.example.aquitabom.data.model.Comment
import com.example.aquitabom.data.repository.CommentRepository
import com.example.aquitabom.data.repository.CommentRepositoryImpl
import kotlinx.coroutines.launch

class CommentsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CommentRepository = CommentRepositoryImpl()
    private val sessionManager = SessionManager(application)

    var comments by mutableStateOf<List<Comment>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isSending by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadComments(postId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            repository.getComments(token, postId).fold(
                onSuccess = { comments = it },
                onFailure = { errorMessage = it.message ?: "Erro ao carregar comentários" }
            )
            isLoading = false
        }
    }

    fun addComment(postId: String, text: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        if (text.isBlank()) return
        viewModelScope.launch {
            isSending = true
            errorMessage = null
            repository.createComment(token, postId, text.trim()).fold(
                onSuccess = { comments = comments + it },
                onFailure = { errorMessage = it.message ?: "Erro ao publicar comentário" }
            )
            isSending = false
        }
    }

    fun deleteComment(commentId: String) {
        val token = sessionManager.fetchAuthToken() ?: return
        viewModelScope.launch {
            repository.deleteComment(token, commentId).fold(
                onSuccess = { comments = comments.filterNot { it.id == commentId } },
                onFailure = { errorMessage = it.message ?: "Erro ao excluir comentário" }
            )
        }
    }
}
