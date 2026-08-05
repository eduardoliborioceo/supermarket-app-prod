package app.supermercado.mobile.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.supermercado.mobile.ui.theme.SupermercadoTheme

/**
 * Placeholder da tela de login. A versao real troca o botao por "Entrar com
 * Google" (unico provedor do app, ver CLAUDE.md > Auth), abrindo o fluxo
 * OAuth do Google e trocando o codigo por um JWT emitido pelo Flask — ver
 * docs/mobile-nativo/PLANO-MIGRACAO-ANDROID-NATIVO.md (Fase 2).
 */
@Composable
fun LoginScreen(onLoginSuccess: () -> Unit) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "Supermercado", style = MaterialTheme.typography.headlineLarge)
            Button(onClick = onLoginSuccess) {
                Text(text = "Entrar com Google")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    SupermercadoTheme {
        LoginScreen(onLoginSuccess = {})
    }
}
