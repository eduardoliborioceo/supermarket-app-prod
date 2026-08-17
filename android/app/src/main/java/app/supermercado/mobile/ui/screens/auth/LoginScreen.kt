package app.supermercado.mobile.ui.screens.auth

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens

/**
 * Único provedor de login é o Google (ver CLAUDE.md > Auth — não existe login
 * por senha neste projeto). O botão abre uma Custom Tab pro fluxo OAuth do
 * Flask; o retorno chega via deep link capturado pela MainActivity e
 * processado pelo LoginViewModel (ver docs/mobile-nativo, Fase 2).
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state) {
        if (state is LoginState.Sucesso) onLoginSuccess()
    }

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

            when (val current = state) {
                is LoginState.Carregando -> CircularProgressIndicator(
                    modifier = Modifier.padding(top = 24.dp),
                    color = SupermercadoColorTokens.primary,
                )
                is LoginState.Erro -> {
                    Text(
                        text = current.mensagem,
                        color = SupermercadoColorTokens.error,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                    BotaoEntrarComGoogle(onClick = {
                        viewModel.iniciarLogin()
                        abrirCustomTab(context, viewModel.urlLoginGoogle)
                    })
                }
                else -> BotaoEntrarComGoogle(onClick = {
                    viewModel.iniciarLogin()
                    abrirCustomTab(context, viewModel.urlLoginGoogle)
                })
            }
        }
    }
}

@Composable
private fun BotaoEntrarComGoogle(onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.padding(top = 24.dp)) {
        Text(text = "Entrar com Google")
    }
}

private fun abrirCustomTab(context: android.content.Context, url: String) {
    val customTabsIntent = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(
            androidx.browser.customtabs.CustomTabColorSchemeParams.Builder()
                .setToolbarColor(SupermercadoColorTokens.sidebarBackground.toArgb())
                .build(),
        )
        .build()

    // Sem isso, o Android pode roteirar a URL pra um WebAPK do PWA instalado
    // no aparelho (mesma origem) em vez de abrir como Custom Tab de verdade
    // vinculada a este app — força a resolução direta pelo navegador padrão.
    val navegador = androidx.browser.customtabs.CustomTabsClient.getPackageName(context, null)
    if (navegador != null) {
        customTabsIntent.intent.setPackage(navegador)
    }

    customTabsIntent.launchUrl(context, url.toUri())
}
