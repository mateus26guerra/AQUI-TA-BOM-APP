package com.example.aquitabom.data.local

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("aquitabom_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val THEME_PREF = "theme_pref"
        const val AVATAR_COLOR = "avatar_color"
    }

    fun saveAvatarColor(color: Int) {
        val editor = prefs.edit()
        editor.putInt(AVATAR_COLOR, color)
        editor.commit() // Uso de commit para garantir a escrita imediata antes de reiniciar
    }

    fun fetchAvatarColor(): Int {
        return prefs.getInt(AVATAR_COLOR, 0xFF3498DB.toInt())
    }

    fun saveThemePreference(theme: String) {
        val editor = prefs.edit()
        editor.putString(THEME_PREF, theme)
        editor.commit() // Uso de commit para garantir a escrita imediata antes de reiniciar
    }

    fun fetchThemePreference(): String {
        return prefs.getString(THEME_PREF, "SYSTEM") ?: "SYSTEM"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.apply()
    }
}
