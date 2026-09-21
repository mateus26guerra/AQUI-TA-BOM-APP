package com.example.aquitabom.ui.theme

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.aquitabom.data.local.SessionManager

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    
    var themePreference by mutableStateOf(sessionManager.fetchThemePreference())
        private set

    fun onThemeChange(newTheme: String) {
        themePreference = newTheme
        sessionManager.saveThemePreference(newTheme)
    }
}
