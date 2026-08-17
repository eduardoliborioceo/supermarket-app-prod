package app.supermercado.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.supermercado.mobile.ui.screens.auth.LoginScreen
import app.supermercado.mobile.ui.screens.autologin.AutoLoginScreen
import app.supermercado.mobile.ui.screens.home.HomeScreen

/**
 * Grafo da Fase 2 do plano de migracao (docs/mobile-nativo): AutoLogin decide
 * entre Home (refresh token salvo, renovado em silencio) e Login (Google
 * OAuth). Cada fase seguinte adiciona seu proprio grafo de feature (produtos,
 * selecionar supermercado, carrinho) aqui.
 */
object SupermercadoDestinations {
    const val AUTO_LOGIN = "auto_login"
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun SupermercadoNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = SupermercadoDestinations.AUTO_LOGIN) {
        composable(SupermercadoDestinations.AUTO_LOGIN) {
            AutoLoginScreen(
                onNavigateToHome = {
                    navController.navigate(SupermercadoDestinations.HOME) {
                        popUpTo(SupermercadoDestinations.AUTO_LOGIN) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(SupermercadoDestinations.LOGIN) {
                        popUpTo(SupermercadoDestinations.AUTO_LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(SupermercadoDestinations.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(SupermercadoDestinations.HOME) {
                        popUpTo(SupermercadoDestinations.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(SupermercadoDestinations.HOME) {
            HomeScreen()
        }
    }
}
