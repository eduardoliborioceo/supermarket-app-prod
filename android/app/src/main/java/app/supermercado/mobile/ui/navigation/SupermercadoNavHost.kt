package app.supermercado.mobile.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.supermercado.mobile.ui.screens.auth.LoginScreen
import app.supermercado.mobile.ui.screens.autologin.AutoLoginScreen
import app.supermercado.mobile.ui.screens.home.HomeScreen
import app.supermercado.mobile.ui.screens.produtos.ProdutosScreen
import app.supermercado.mobile.ui.screens.supermercado.SelecionarSupermercadoScreen

/**
 * Grafo da Fase 2/3 do plano de migracao (docs/mobile-nativo): AutoLogin
 * decide entre Home (refresh token salvo, renovado em silencio) e Login
 * (Google OAuth). Paridade de feature com o web (Fase 3) completa com
 * Produtos e Selecionar Supermercado — navegacao ad-hoc (botoes no TopAppBar
 * da Home) ate essas telas, sem menu inferior por enquanto (poucas abas pra
 * justificar uma bottom nav ainda).
 */
object SupermercadoDestinations {
    const val AUTO_LOGIN = "auto_login"
    const val LOGIN = "login"
    const val HOME = "home"
    const val PRODUTOS = "produtos"
    const val SUPERMERCADO = "supermercado"
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
            HomeScreen(
                onAbrirProdutos = { navController.navigate(SupermercadoDestinations.PRODUTOS) },
                onAbrirSupermercado = { navController.navigate(SupermercadoDestinations.SUPERMERCADO) },
            )
        }
        composable(SupermercadoDestinations.PRODUTOS) {
            ProdutosScreen(onVoltar = { navController.popBackStack() })
        }
        composable(SupermercadoDestinations.SUPERMERCADO) {
            SelecionarSupermercadoScreen(onVoltar = { navController.popBackStack() })
        }
    }
}
