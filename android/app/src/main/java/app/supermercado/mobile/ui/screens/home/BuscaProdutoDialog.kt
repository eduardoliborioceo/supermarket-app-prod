package app.supermercado.mobile.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.supermercado.mobile.core.data.produtos.ProdutoAdminDto
import app.supermercado.mobile.core.util.formatarMoeda
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens

/**
 * Busca em todo o catalogo de produtos cadastrados (nao so os que ja estao
 * na lista de compras de hoje) — quando tem muito produto cadastrado, achar
 * um pra editar rolando categoria por categoria na tela Produtos fica ruim.
 * Ao tocar num resultado, [onProdutoSelecionado] navega pra aba Produtos e
 * ja abre/destaca aquele item la.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuscaProdutoDialog(
    onDismiss: () -> Unit,
    onProdutoSelecionado: (Int) -> Unit,
    viewModel: BuscaProdutoViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    val resultados = remember(query, state.produtos) {
        val qNorm = normalizarBusca(query.trim())
        if (qNorm.isEmpty()) emptyList() else state.produtos.filter { normalizarBusca(it.nome).contains(qNorm) }
    }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.fillMaxSize(), color = SupermercadoColorTokens.background) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(SupermercadoColorTokens.surface).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Fechar busca")
                    }
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar produto...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = SupermercadoColorTokens.background,
                            focusedContainerColor = SupermercadoColorTokens.background,
                        ),
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.weight(1f).focusRequester(focusRequester),
                    )
                }

                when {
                    state.carregando -> Box(modifier = Modifier.fillMaxSize()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = SupermercadoColorTokens.primary,
                        )
                    }
                    state.erro != null -> MensagemCentralizada(state.erro!!)
                    query.isBlank() -> MensagemCentralizada("Digite pra buscar em todos os produtos cadastrados.")
                    resultados.isEmpty() -> MensagemCentralizada("Nenhum produto encontrado para \"$query\".")
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                    ) {
                        items(resultados, key = { it.id }) { produto ->
                            ResultadoBuscaItem(produto = produto, onClick = { onProdutoSelecionado(produto.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MensagemCentralizada(texto: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(texto, color = SupermercadoColorTokens.onSurfaceMuted, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ResultadoBuscaItem(produto: ProdutoAdminDto, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = BorderStroke(1.dp, SupermercadoColorTokens.border),
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(produto.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(produto.setor, style = MaterialTheme.typography.labelMedium, color = SupermercadoColorTokens.onSurfaceMuted)
            }
            Text(
                formatarMoeda(produto.ultimoPreco),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = SupermercadoColorTokens.primary,
            )
        }
    }
}
