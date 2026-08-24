package app.supermercado.mobile.ui.screens.mais

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.supermercado.mobile.BuildConfig
import app.supermercado.mobile.ui.components.PillBadge
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens

private val CardRadius = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaisScreen(onLogout: () -> Unit, viewModel: MaisViewModel = hiltViewModel()) {
    val saindo by viewModel.saindo.collectAsStateWithLifecycle()
    var confirmarSairAberto by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SupermercadoColorTokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Mais", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SupermercadoColorTokens.surface),
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(SupermercadoColorTokens.background).padding(innerPadding)) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    shape = CardRadius,
                    colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
                    border = BorderStroke(1.dp, SupermercadoColorTokens.border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    Column {
                        MaisItemRow(
                            icon = Icons.Filled.History,
                            titulo = "Histórico",
                            habilitado = false,
                        )
                        HorizontalDivider(color = SupermercadoColorTokens.border)
                        MaisItemRow(
                            icon = Icons.Filled.Settings,
                            titulo = "Configurações",
                            habilitado = false,
                        )
                    }
                }

                Card(
                    shape = CardRadius,
                    colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
                    border = BorderStroke(1.dp, SupermercadoColorTokens.border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                ) {
                    MaisItemRow(
                        icon = Icons.AutoMirrored.Filled.Logout,
                        titulo = "Sair",
                        corConteudo = SupermercadoColorTokens.error,
                        carregando = saindo,
                        onClick = { confirmarSairAberto = true },
                    )
                }

                Text(
                    "Versão ${BuildConfig.VERSION_NAME}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SupermercadoColorTokens.onSurfaceMuted,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    if (confirmarSairAberto) {
        AlertDialog(
            onDismissRequest = { confirmarSairAberto = false },
            title = { Text("Sair da conta") },
            text = { Text("Você precisará entrar novamente com sua conta Google para acessar sua lista.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarSairAberto = false
                    viewModel.sair(onLogout)
                }) { Text("Sair", color = SupermercadoColorTokens.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarSairAberto = false }) { Text("Cancelar") }
            },
        )
    }
}

@Composable
private fun MaisItemRow(
    icon: ImageVector,
    titulo: String,
    modifier: Modifier = Modifier,
    corConteudo: Color = SupermercadoColorTokens.onSurface,
    habilitado: Boolean = true,
    carregando: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null && habilitado && !carregando,
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (habilitado) corConteudo else SupermercadoColorTokens.onSurfaceMuted,
            )
            Text(
                titulo,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (habilitado) corConteudo else SupermercadoColorTokens.onSurfaceMuted,
                modifier = Modifier.weight(1f),
            )
            when {
                carregando -> CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = corConteudo)
                !habilitado -> PillBadge(texto = "Em breve")
                onClick != null -> Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = SupermercadoColorTokens.onSurfaceMuted,
                )
            }
        }
    }
}
