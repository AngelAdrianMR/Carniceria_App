package com.example.carniceria_app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun NavGraph(
    navController: NavHostController,
    onGoogleSignInClick: () -> Unit,
    onLogout: () -> Unit // 👈 NUEVO PARÁMETRO
) {
    NavHost(navController, startDestination = "login") {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { navController.navigate("homeUserScreen") },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.navigate("homeUserScreen") },
                onBackToLogin = { navController.popBackStack() },
                onGoogleSignInClick = onGoogleSignInClick
            )
        }

        composable("homeUserScreen") {
            HomeUserScreen(
                navController = navController,
                onLogout = onLogout
            )
        }

        composable("homeAdminScreen") {
            HomeAdminScreen(
                navController = navController,
                onLogout = onLogout
            )
        }
    }
}
