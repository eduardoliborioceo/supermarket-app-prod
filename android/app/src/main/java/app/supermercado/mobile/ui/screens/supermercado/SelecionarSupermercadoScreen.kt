package app.supermercado.mobile.ui.screens.supermercado

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.supermercado.mobile.core.data.supermercado.ResultadoOperacaoSupermercado
import app.supermercado.mobile.core.util.obterLocalizacaoAtual
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private val SheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
private val ResultShape = RoundedCornerShape(14.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelecionarSupermercadoScreen(
    onVoltar: () -> Unit,
    onSelecionado: () -> Unit = onVoltar,
    viewModel: SelecionarSupermercadoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var mensagemErro by remember { mutableStateOf<String?>(null) }

    fun buscarPorGps() {
        viewModel.iniciarBuscaLocalizacao()
        coroutineScope.launch {
            val localizacao = withTimeoutOrNull(8000) { obterLocalizacaoAtual(context) }
            if (localizacao != null) {
                viewModel.buscarPorLocalizacao(localizacao.latitude, localizacao.longitude)
            } else {
                viewModel.erroLocalizacao("Não foi possível obter a localização. Permita o acesso ao GPS.")
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedida ->
        if (concedida) {
            buscarPorGps()
        } else {
            viewModel.erroLocalizacao("Permita o acesso à localização para buscar mercados próximos.")
        }
    }

    fun onGpsClick() {
        val temPermissao = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (temPermissao) buscarPorGps() else permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    fun selecionar(nome: String, endereco: String, placeId: String) {
        viewModel.selecionar(nome, endereco, placeId) { resultado ->
            when (resultado) {
                is ResultadoOperacaoSupermercado.Sucesso -> onSelecionado()
                is ResultadoOperacaoSupermercado.Erro -> mensagemErro = resultado.mensagem
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(SupermercadoColorTokens.sidebarBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
            }
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)) {
                Text(
                    "Onde você vai\nfazer compras hoje?",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = SupermercadoColorTokens.background,
                shape = SheetShape,
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        state.carregando -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = SupermercadoColorTokens.primary,
                        )
                        state.erro != null -> Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Text(state.erro!!, color = SupermercadoColorTokens.onSurfaceMuted)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = viewModel::carregar) { Text("Tentar novamente") }
                        }
                        else -> Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                        ) {
                            if (!state.atualNome.isNullOrBlank()) {
                                UltimaSelecaoCard(
                                    nome = state.atualNome!!,
                                    endereco = state.atualEndereco,
                                    salvando = state.salvando,
                                    onUsar = { selecionar(state.atualNome!!, state.atualEndereco ?: "", state.atualPlaceId) },
                                )
                            }

                            if (state.hasApi) {
                                BuscaSupermercado(
                                    query = state.query,
                                    buscando = state.buscando || state.obtendoLocalizacao,
                                    onQueryChange = viewModel::alterarQuery,
                                    onGpsClick = { onGpsClick() },
                                )

                                state.hint?.let {
                                    Text(it, style = MaterialTheme.typography.labelMedium, color = SupermercadoColorTokens.onSurfaceMuted)
                                }

                                if (state.resultados.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        state.resultados.forEach { resultado ->
                                            ResultadoCard(
                                                resultado = resultado,
                                                salvando = state.salvando,
                                                onUsar = { selecionar(resultado.nome, resultado.endereco, resultado.placeId) },
                                            )
                                        }
                                    }
                                }
                            } else {
                                EntradaManual(salvando = state.salvando, onConfirmar = { nome -> selecionar(nome, "", "") })
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }

    mensagemErro?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensagemErro = null },
            confirmButton = { TextButton(onClick = { mensagemErro = null }) { Text("OK") } },
            title = { Text("Ops") },
            text = { Text(texto) },
        )
    }
}

@Composable
private fun UltimaSelecaoCard(nome: String, endereco: String?, salvando: Boolean, onUsar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = BorderStroke(2.dp, SupermercadoColorTokens.primary),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SupermercadoColorTokens.primary, modifier = Modifier.size(16.dp))
                Text(
                    "ÚLTIMA SELEÇÃO",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = SupermercadoColorTokens.primary,
                )
            }
            Column {
                Text(nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (!endereco.isNullOrBlank()) {
                    Text(endereco, style = MaterialTheme.typography.bodyMedium, color = SupermercadoColorTokens.onSurfaceMuted)
                }
            }
            Button(
                onClick = onUsar,
                enabled = !salvando,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SupermercadoColorTokens.sidebarBackground),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Usar este")
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun BuscaSupermercado(
    query: String,
    buscando: Boolean,
    onQueryChange: (String) -> Unit,
    onGpsClick: () -> Unit,
) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            placeholder = { Text("Buscar supermercado...") },
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.weight(1f),
        )
        Surface(
            onClick = onGpsClick,
            enabled = !buscando,
            modifier = Modifier.size(52.dp),
            shape = MaterialTheme.shapes.medium,
            color = SupermercadoColorTokens.sidebarBackground,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                if (buscando) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Filled.MyLocation, contentDescription = "Usar minha localização", tint = Color.White)
                }
            }
        }
    }
}

@Composable
private fun ResultadoCard(resultado: SupermercadoResultadoUi, salvando: Boolean, onUsar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = BorderStroke(1.dp, SupermercadoColorTokens.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = ResultShape,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = SupermercadoColorTokens.primary.copy(alpha = 0.10f),
                modifier = Modifier.size(40.dp),
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(Icons.Filled.Store, contentDescription = null, tint = SupermercadoColorTokens.primary, modifier = Modifier.size(18.dp))
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(resultado.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                if (resultado.endereco.isNotBlank()) {
                    Text(resultado.endereco, style = MaterialTheme.typography.bodySmall, color = SupermercadoColorTokens.onSurfaceMuted)
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    resultado.rating?.let {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(13.dp), tint = SupermercadoColorTokens.categoryColor(1))
                            Text(" $it", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = SupermercadoColorTokens.onSurfaceMuted)
                        }
                    }
                    when (resultado.aberto) {
                        true -> AbertoFechadoBadge("Aberto", SupermercadoColorTokens.success)
                        false -> AbertoFechadoBadge("Fechado", SupermercadoColorTokens.error)
                        null -> {}
                    }
                }
            }
            Button(
                onClick = onUsar,
                enabled = !salvando,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SupermercadoColorTokens.sidebarBackground),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text("Usar", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun AbertoFechadoBadge(texto: String, cor: Color) {
    Surface(shape = RoundedCornerShape(6.dp), color = cor.copy(alpha = 0.10f)) {
        Text(
            texto,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = cor,
        )
    }
}

@Composable
private fun EntradaManual(salvando: Boolean, onConfirmar: (String) -> Unit) {
    var nome by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome do supermercado") },
            placeholder = { Text("Ex: Supermercado Ideal") },
            singleLine = true,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = { onConfirmar(nome.trim()) },
            enabled = !salvando && nome.isNotBlank(),
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Confirmar")
        }
    }
}
