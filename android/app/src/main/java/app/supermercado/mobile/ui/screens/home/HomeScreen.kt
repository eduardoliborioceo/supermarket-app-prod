package app.supermercado.mobile.ui.screens.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.supermercado.mobile.ui.theme.SupermercadoTheme

/**
 * Placeholder da tela Home (lista de compras) — a primeira feature real do
 * plano de migracao. Versao final renderiza os produtos agrupados por
 * categoria/setor com os cards de app/templates/home.html, alimentados por
 * GET /api/produtos (ou equivalente) via um ProdutoRepository + HomeViewModel.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Lista de Compras") }) },
    ) { innerPadding ->
        Text(
            text = "Em construcao — ver Fase 2 do plano de migracao.",
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    SupermercadoTheme {
        HomeScreen()
    }
}
