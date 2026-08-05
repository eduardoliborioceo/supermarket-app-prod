package app.supermercado.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.supermercado.mobile.ui.screens.auth.LoginScreen
import app.supermercado.mobile.ui.screens.home.HomeScreen

/**
 * Grafo placeholder da Fase 1 do plano de migracao (docs/mobile-nativo):
 * Login -> Home (lista de compras). Cada fase seguinte adiciona seu proprio
 * grafo de feature (produtos, selecionar supermercado, carrinho) aqui.
 */
object SupermercadoDestinations {
    const val LOGIN = "login"
    const val HOME = "home"
}

@Composable
fun SupermercadoNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = SupermercadoDestinations.LOGIN) {
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
