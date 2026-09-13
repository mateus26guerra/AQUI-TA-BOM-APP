package com.example.aquitabom.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aquitabom.ui.feed.FeedScreen
import com.example.aquitabom.ui.home.HomeScreen
import com.example.aquitabom.ui.map.MapScreen

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Feed : Screen("feed", Icons.Default.Home, "Home")
    object Map : Screen("map_main", Icons.Default.Search, "Mapa")
    object Add : Screen("add", Icons.Default.Add, "Postar")
    object Profile : Screen("profile", Icons.Default.Person, "Perfil")
    object Settings : Screen("settings", Icons.Default.Settings, "Ajustes")
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Feed,
        Screen.Map,
        Screen.Add,
        Screen.Profile,
        Screen.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = Color(0xFFE67E22)
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFE67E22),
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Feed.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Feed.route) { FeedScreen() }
            composable(Screen.Map.route) { MapScreen() }
            composable(Screen.Add.route) { HomeScreen() } // Mocking for now
            composable(Screen.Profile.route) { HomeScreen() } // Mocking for now
            composable(Screen.Settings.route) { HomeScreen() } // Mocking for now
        }
    }
}
