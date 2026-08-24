package app.supermercado.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.supermercado.mobile.ui.screens.auth.LoginScreen
import app.supermercado.mobile.ui.screens.autologin.AutoLoginScreen

/**
 * Grafo raiz do app: AutoLogin decide entre Main (refresh token salvo,
 * renovado em silencio) e Login (Google OAuth). Fase 4 do plano de migracao
 * (docs/mobile-nativo) troca a navegacao ad-hoc entre Home/Produtos/
 * Supermercado por abas fixas dentro de MainScreen (bottom nav, paridade
 * com base.html), incluindo a nova aba "Mais" com o logout.
 */
object SupermercadoDestinations {
    const val AUTO_LOGIN = "auto_login"
    const val LOGIN = "login"
    const val MAIN = "main"
}

@Composable
fun SupermercadoNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = SupermercadoDestinations.AUTO_LOGIN) {
        composable(SupermercadoDestinations.AUTO_LOGIN) {
            AutoLoginScreen(
                onNavigateToHome = {
                    navController.navigate(SupermercadoDestinations.MAIN) {
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
                    navController.navigate(SupermercadoDestinations.MAIN) {
                        popUpTo(SupermercadoDestinations.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(SupermercadoDestinations.MAIN) {
            MainScreen(
                onLogout = {
                    navController.navigate(SupermercadoDestinations.LOGIN) {
                        popUpTo(SupermercadoDestinations.MAIN) { inclusive = true }
                    }
                },
            )
        }
    }
}
