package com.palabradeldia.presentation.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.palabradeldia.presentation.ui.favorites.FavoritesScreen
import com.palabradeldia.presentation.ui.home.HomeScreen
import com.palabradeldia.presentation.ui.settings.SettingsScreen

/** App-level destinations. */
sealed class Screen(val route: String) {
    data object Home      : Screen("home")
    data object Favorites : Screen("favorites")
    data object Settings  : Screen("settings")
}

/**
 * Root navigation host. Starts at [Screen.Home].
 */
@Composable
fun PalabraNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController  = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToFavorites = { navController.navigate(Screen.Favorites.route) },
                onNavigateToSettings  = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
