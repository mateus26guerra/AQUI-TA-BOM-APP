package com.example.aquitabom.ui.theme

import android.app.Application
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import com.example.aquitabom.data.local.SessionManager

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    
    var themePreference by mutableStateOf(sessionManager.fetchThemePreference())
        private set

    var avatarColor by mutableIntStateOf(sessionManager.fetchAvatarColor())
        private set

    fun onThemeChange(newTheme: String) {
        themePreference = newTheme
        sessionManager.saveThemePreference(newTheme)
    }

    fun onAvatarColorChange(newColor: Int) {
        avatarColor = newColor
        sessionManager.saveAvatarColor(newColor)
    }
}
