package app.supermercado.mobile.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.supermercado.mobile.ui.screens.home.HomeScreen
import app.supermercado.mobile.ui.screens.mais.MaisScreen
import app.supermercado.mobile.ui.screens.produtos.ProdutosScreen
import app.supermercado.mobile.ui.screens.supermercado.SelecionarSupermercadoScreen
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens

/**
 * Abas persistentes da Fase 4 (paridade com a bottom-nav do PWA em
 * base.html): Home, Produtos e Supermercado passam de navegação ad-hoc
 * (botões no TopAppBar) para abas fixas; "Mais" é nova, sem equivalente
 * roteado na web hoje, e concentra Sair (ação que antes não existia
 * em lugar nenhum do app nativo).
 */
private object MainDestinations {
    const val HOME = "main_home"
    const val PRODUTOS = "main_produtos"
    const val SUPERMERCADO = "main_supermercado"
    const val MAIS = "main_mais"
}

private data class BottomTabItem(val route: String, val label: String, val icon: ImageVector)

private val bottomTabs = listOf(
    BottomTabItem(MainDestinations.HOME, "Compras", Icons.Filled.ShoppingCart),
    BottomTabItem(MainDestinations.PRODUTOS, "Produtos", Icons.AutoMirrored.Filled.List),
    BottomTabItem(MainDestinations.SUPERMERCADO, "Mercado", Icons.Filled.Store),
    BottomTabItem(MainDestinations.MAIS, "Mais", Icons.Filled.MoreHoriz),
)

@Composable
fun MainScreen(onLogout: () -> Unit, navController: NavHostController = rememberNavController()) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = SupermercadoColorTokens.background,
        bottomBar = {
            NavigationBar(containerColor = SupermercadoColorTokens.sidebarBackground) {
                bottomTabs.forEach { tab ->
                    NavigationBarItem(
                        selected = currentRoute == tab.route,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = SupermercadoColorTokens.sidebarActiveAccent,
                            selectedTextColor = SupermercadoColorTokens.sidebarActiveAccent,
                            indicatorColor = SupermercadoColorTokens.sidebarActiveBackground,
                            unselectedIconColor = SupermercadoColorTokens.sidebarText,
                            unselectedTextColor = SupermercadoColorTokens.sidebarText,
                        ),
                    )
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = MainDestinations.HOME,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(MainDestinations.HOME) { HomeScreen() }
            composable(MainDestinations.PRODUTOS) { ProdutosScreen() }
            composable(MainDestinations.SUPERMERCADO) {
                SelecionarSupermercadoScreen(
                    onSelecionado = {
                        navController.navigate(MainDestinations.HOME) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
            composable(MainDestinations.MAIS) { MaisScreen(onLogout = onLogout) }
        }
    }
}
