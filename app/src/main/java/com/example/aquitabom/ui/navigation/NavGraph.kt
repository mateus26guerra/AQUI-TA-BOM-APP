package com.example.aquitabom.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.aquitabom.data.local.AuthEventBus
import com.example.aquitabom.data.local.SessionManager
import com.example.aquitabom.ui.MainScreen
import com.example.aquitabom.ui.login.LoginScreen
import com.example.aquitabom.ui.register.RegisterScreen
import com.example.aquitabom.ui.theme.ThemeViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NavGraph(themeViewModel: ThemeViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    // Escuta eventos de "Não Autenticado" (401) globalmente
    LaunchedEffect(Unit) {
        AuthEventBus.unauthorizedEvent.collectLatest {
            sessionManager.clearAuthToken()
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    
    // Define o destino inicial com base na existência do token
    val startDestination = if (sessionManager.fetchAuthToken() != null) "main" else "login"
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
                onBackToLogin = {
                    navController.popBackStack()
                }
            )
        }
        composable("main") {
            MainScreen(
                onLogout = {
                    sessionManager.clearAuthToken()
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                },
                themeViewModel = themeViewModel
            )
        }
    }
}
