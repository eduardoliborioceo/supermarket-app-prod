package app.supermercado.mobile.ui.screens.auth

import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.supermercado.mobile.R
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

    Box(
        modifier = Modifier.fillMaxSize().background(SupermercadoColorTokens.background),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_supermercado_logo_splash),
                    contentDescription = "Supermercado",
                    modifier = Modifier.size(72.dp),
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Supermercado",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Organize sua lista de compras\nde forma rápida e simples.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SupermercadoColorTokens.onSurfaceMuted,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(32.dp))

                when (val current = state) {
                    is LoginState.Carregando -> CircularProgressIndicator(color = SupermercadoColorTokens.primary)
                    is LoginState.Erro -> {
                        Text(
                            text = current.mensagem,
                            color = SupermercadoColorTokens.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(bottom = 16.dp),
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
}

@Composable
private fun BotaoEntrarComGoogle(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SupermercadoColorTokens.surface,
            contentColor = SupermercadoColorTokens.onSurface,
        ),
        border = BorderStroke(1.dp, SupermercadoColorTokens.border),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
        contentPadding = PaddingValues(vertical = 13.dp, horizontal = 20.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_google_logo),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Entrar com Google", fontWeight = FontWeight.SemiBold)
        }
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
