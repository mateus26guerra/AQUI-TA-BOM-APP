package com.example.aquitabom.ui.register

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.aquitabom.data.model.RegisterRequest
import com.example.aquitabom.data.repository.AuthRepository
import com.example.aquitabom.data.repository.AuthRepositoryImpl
import kotlinx.coroutines.launch

sealed class RegisterUiState {
    object Idle : RegisterUiState()
    object Loading : RegisterUiState()
    object Success : RegisterUiState()
    data class Error(val message: String) : RegisterUiState()
}

class RegisterViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    var nome by mutableStateOf("")
    var email by mutableStateOf("")
    var senha by mutableStateOf("")
    var uiState by mutableStateOf<RegisterUiState>(RegisterUiState.Idle)
        private set

    fun onRegisterClick() {
        if (nome.isBlank() || email.isBlank() || senha.isBlank()) {
            uiState = RegisterUiState.Error("Preencha todos os campos")
            return
        }

        viewModelScope.launch {
            uiState = RegisterUiState.Loading
            val result = repository.register(RegisterRequest(nome, email, senha))
            uiState = result.fold(
                onSuccess = { RegisterUiState.Success },
                onFailure = { RegisterUiState.Error(it.message ?: "Erro ao cadastrar") }
            )
        }
    }
}
