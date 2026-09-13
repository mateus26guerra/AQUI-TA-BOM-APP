package com.example.aquitabom.ui.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquitabom.data.model.LoginRequest
import com.example.aquitabom.data.repository.AuthRepository
import com.example.aquitabom.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.launch

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val token: String) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

class LoginViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    var email by mutableStateOf("")
    var senha by mutableStateOf("")
    var uiState by mutableStateOf<LoginUiState>(LoginUiState.Idle)
        private set

    fun onLoginClick() {
        if (email.isBlank() || senha.isBlank()) {
            uiState = LoginUiState.Error("Preencha todos os campos")
            return
        }

        viewModelScope.launch {
            uiState = LoginUiState.Loading
            val result = repository.login(LoginRequest(email, senha))
            uiState = result.fold(
                onSuccess = { LoginUiState.Success(it.accessToken) },
                onFailure = { LoginUiState.Error(it.message ?: "Erro desconhecido") }
            )
        }
    }
}
