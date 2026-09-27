package com.example.aquitabom.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.aquitabom.data.local.SessionManager
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.aquitabom.data.model.Restaurant
import com.example.aquitabom.ui.feed.FeedScreen
import com.example.aquitabom.ui.map.MapScreen
import com.example.aquitabom.ui.post.CreatePostScreen
import com.example.aquitabom.ui.profile.ProfileScreen
import com.example.aquitabom.ui.restaurant.RestaurantDetailScreen
import com.example.aquitabom.ui.settings.SettingsScreen
import com.example.aquitabom.ui.theme.ThemeViewModel
import com.example.aquitabom.data.repository.RestaurantRepository
import com.example.aquitabom.data.repository.RestaurantRepositoryImpl
import kotlinx.coroutines.launch

sealed class Screen(val route: String, val icon: ImageVector, val label: String) {
    object Feed : Screen("feed", Icons.Default.Home, "Home")
    object Map : Screen("map_main", Icons.Default.Search, "Mapa")
    object Add : Screen("add", Icons.Default.Add, "Postar")
    object Profile : Screen("profile", Icons.Default.Person, "Perfil")
    object Settings : Screen("settings", Icons.Default.Settings, "Ajustes")
    object RestaurantDetail : Screen("restaurant_detail", Icons.Default.Restaurant, "Detalhes")
}

@Composable
fun MainScreen(
    onLogout: () -> Unit,
    themeViewModel: ThemeViewModel
) {
    val navController = rememberNavController()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    var selectedRestaurantForDetail by remember { mutableStateOf<Restaurant?>(null) }
    var selectedRestaurantId by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val restaurantRepository: RestaurantRepository = remember { RestaurantRepositoryImpl() }
    
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
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
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
                            // O ícone selecionado agora fica Preto no Modo Claro e Branco no Modo Escuro
                            // Isso remove o "Laranja" constante do menu inferior.
                            selectedIconColor = MaterialTheme.colorScheme.onSurface,
                            unselectedIconColor = Color.Gray,
                            indicatorColor = Color.Transparent, // Sem fundo colorido atrás do ícone
                            selectedTextColor = MaterialTheme.colorScheme.onSurface,
                            unselectedTextColor = Color.Gray
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
            composable(Screen.Feed.route) { 
                FeedScreen(
                    themeViewModel = themeViewModel,
                    onRestaurantClick = { restaurantName ->
                        scope.launch {
                            val token = sessionManager.fetchAuthToken()
                            if (token != null) {
                                restaurantRepository.getNearbyRestaurants(token).onSuccess { restaurants ->
                                    restaurants.firstOrNull {
                                        it.nome.equals(restaurantName, ignoreCase = true)
                                    }?.let { restaurant ->
                                        selectedRestaurantForDetail = restaurant
                                        selectedRestaurantId = restaurant.id
                                        navController.navigate(Screen.RestaurantDetail.route)
                                    }
                                }
                            }
                        }
                    }
                ) 
            }
            composable(Screen.Map.route) { 
                MapScreen(
                    isVisible = currentRoute == Screen.Map.route,
                    onRestaurantClick = { restaurant ->
                        selectedRestaurantForDetail = restaurant
                        selectedRestaurantId = restaurant.id
                        navController.navigate(Screen.RestaurantDetail.route)
                    }
                ) 
            }
            composable(Screen.Add.route) { 
                CreatePostScreen(
                    onPostCreated = {
                        navController.navigate(Screen.Feed.route) {
                            popUpTo(Screen.Feed.route) { inclusive = true }
                        }
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Profile.route) { 
                ProfileScreen(
                    themeViewModel = themeViewModel,
                    onRestaurantClick = { restaurantName ->
                        scope.launch {
                            val token = sessionManager.fetchAuthToken()
                            if (token != null) {
                                restaurantRepository.getNearbyRestaurants(token).onSuccess { restaurants ->
                                    restaurants.firstOrNull {
                                        it.nome.equals(restaurantName, ignoreCase = true)
                                    }?.let { restaurant ->
                                        selectedRestaurantForDetail = restaurant
                                        selectedRestaurantId = restaurant.id
                                        navController.navigate(Screen.RestaurantDetail.route)
                                    }
                                }
                            }
                        }
                    }
                ) 
            }
            composable(Screen.Settings.route) { 
                SettingsScreen(
                    onLogout = onLogout,
                    themeViewModel = themeViewModel
                ) 
            }
            composable(Screen.RestaurantDetail.route) {
                LaunchedEffect(selectedRestaurantId) {
                    if (selectedRestaurantForDetail == null && selectedRestaurantId != null) {
                        val token = sessionManager.fetchAuthToken()
                        if (token != null) {
                            restaurantRepository.getNearbyRestaurants(token).onSuccess { restaurants ->
                                selectedRestaurantForDetail = restaurants.firstOrNull {
                                    it.id == selectedRestaurantId
                                }
                            }
                        }
                    }
                }

                selectedRestaurantForDetail?.let { restaurant ->
                    RestaurantDetailScreen(
                        restaurant = restaurant,
                        onBack = {
                            selectedRestaurantForDetail = null
                            selectedRestaurantId = null
                            navController.popBackStack()
                        }
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
