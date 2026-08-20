package app.supermercado.mobile.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import app.supermercado.mobile.core.util.formatarMoeda
import app.supermercado.mobile.core.util.parseMoeda
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onAbrirProdutos: () -> Unit = {},
    onAbrirSupermercado: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialogAdicionarAberto by rememberSaveable { mutableStateOf(false) }
    var categoriaParaNovoProduto by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmarLimparAberto by rememberSaveable { mutableStateOf(false) }
    var mensagemErroAcao by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lista de Compras") },
                actions = {
                    IconButton(onClick = onAbrirSupermercado) {
                        Icon(Icons.Filled.Store, contentDescription = "Selecionar supermercado")
                    }
                    IconButton(onClick = onAbrirProdutos) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Produtos")
                    }
                    IconButton(onClick = { confirmarLimparAberto = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Limpar carrinho")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                categoriaParaNovoProduto = null
                dialogAdicionarAberto = true
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Cadastrar produto")
            }
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when {
                state.carregando -> CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = SupermercadoColorTokens.primary,
                )
                state.erro != null -> ErroCarregarHome(mensagem = state.erro!!, onTentarNovamente = viewModel::carregar)
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item {
                        KpiRow(
                            totalAtual = state.totalAtual,
                            gastoPrevisto = state.gastoPrevisto,
                            saldoDisponivel = state.saldoDisponivel,
                            onGastoPrevistoChange = viewModel::alterarGastoPrevisto,
                        )
                    }
                    items(state.categorias, key = { it.nome }) { categoria ->
                        CategoriaSection(
                            categoria = categoria,
                            onQtdChange = viewModel::alterarQuantidade,
                            onPrecoChange = viewModel::alterarPreco,
                            onAdicionarClick = {
                                categoriaParaNovoProduto = categoria.nome
                                dialogAdicionarAberto = true
                            },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(72.dp)) }
                }
            }
        }
    }

    if (dialogAdicionarAberto) {
        AdicionarProdutoDialog(
            categorias = state.categorias.map { it.nome },
            categoriaInicial = categoriaParaNovoProduto,
            onDismiss = { dialogAdicionarAberto = false },
            onConfirmar = { nome, preco, setor ->
                viewModel.adicionarProduto(nome, preco, setor) { resultado ->
                    if (resultado is app.supermercado.mobile.core.data.produtos.ResultadoAdicionarProduto.Erro) {
                        mensagemErroAcao = resultado.mensagem
                    } else {
                        dialogAdicionarAberto = false
                    }
                }
            },
        )
    }

    if (confirmarLimparAberto) {
        AlertDialog(
            onDismissRequest = { confirmarLimparAberto = false },
            title = { Text("Limpar dados") },
            text = { Text("Deseja limpar todos os dados da compra atual?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarLimparAberto = false
                    viewModel.limparCarrinho { mensagemErroAcao = it }
                }) { Text("Limpar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimparAberto = false }) { Text("Cancelar") }
            },
        )
    }

    mensagemErroAcao?.let { mensagem ->
        AlertDialog(
            onDismissRequest = { mensagemErroAcao = null },
            title = { Text("Ops") },
            text = { Text(mensagem) },
            confirmButton = {
                TextButton(onClick = { mensagemErroAcao = null }) { Text("Entendi") }
            },
        )
    }
}

@Composable
private fun ErroCarregarHome(mensagem: String, onTentarNovamente: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = mensagem, color = SupermercadoColorTokens.onSurfaceMuted)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onTentarNovamente) { Text("Tentar novamente") }
    }
}

@Composable
private fun KpiRow(
    totalAtual: Double,
    gastoPrevisto: Double,
    saldoDisponivel: Double,
    onGastoPrevistoChange: (Double) -> Unit,
) {
    var textoGasto by rememberSaveable(gastoPrevisto) {
        mutableStateOf(if (gastoPrevisto == 0.0) "" else formatarMoeda(gastoPrevisto).removePrefix("R$ "))
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            KpiCard(titulo = "Total atual", valor = formatarMoeda(totalAtual), modifier = Modifier.weight(1f))
            KpiCard(
                titulo = "Gasto previsto",
                modifier = Modifier.weight(1f),
                conteudo = {
                    OutlinedTextField(
                        value = textoGasto,
                        onValueChange = {
                            textoGasto = it
                            onGastoPrevistoChange(parseMoeda(it))
                        },
                        placeholder = { Text("0,00") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.primary.copy(alpha = 0.08f)),
            shape = MaterialTheme.shapes.large,
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Saldo disponível", style = MaterialTheme.typography.labelMedium, color = SupermercadoColorTokens.onSurfaceMuted)
                Text(formatarMoeda(saldoDisponivel), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun KpiCard(
    titulo: String,
    modifier: Modifier = Modifier,
    valor: String? = null,
    conteudo: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SupermercadoColorTokens.border),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = SupermercadoColorTokens.onSurfaceMuted)
            Spacer(modifier = Modifier.height(4.dp))
            if (valor != null) {
                Text(valor, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            conteudo?.invoke()
        }
    }
}

@Composable
private fun CategoriaSection(
    categoria: CategoriaUi,
    onQtdChange: (Int, Int) -> Unit,
    onPrecoChange: (Int, Double) -> Unit,
    onAdicionarClick: () -> Unit,
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(categoria.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    "${categoria.produtos.size} itens · ${formatarMoeda(categoria.total)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = SupermercadoColorTokens.onSurfaceMuted,
                )
            }
            IconButton(onClick = onAdicionarClick) {
                Icon(Icons.Filled.Add, contentDescription = "Adicionar item em ${categoria.nome}")
            }
        }

        if (categoria.produtos.isEmpty()) {
            Text(
                "Nenhum item nesta categoria. Use o botão \"+\" para adicionar.",
                style = MaterialTheme.typography.bodyMedium,
                color = SupermercadoColorTokens.onSurfaceMuted,
                modifier = Modifier.padding(vertical = 12.dp),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categoria.produtos.forEach { produto ->
                    ProdutoCard(
                        produto = produto,
                        onQtdChange = { delta -> onQtdChange(produto.id, delta) },
                        onPrecoChange = { novoPreco -> onPrecoChange(produto.id, novoPreco) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ProdutoCard(
    produto: ProdutoUi,
    onQtdChange: (Int) -> Unit,
    onPrecoChange: (Double) -> Unit,
) {
    var editandoPreco by remember { mutableStateOf(false) }
    var textoPreco by remember(produto.precoCarrinho) { mutableStateOf(produto.precoCarrinho.toString()) }

    Card(
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, SupermercadoColorTokens.border),
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(produto.nome, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)

                    if (editandoPreco) {
                        OutlinedTextField(
                            value = textoPreco,
                            onValueChange = { textoPreco = it },
                            singleLine = true,
                            modifier = Modifier.width(120.dp),
                        )
                        TextButton(onClick = {
                            editandoPreco = false
                            onPrecoChange(parseMoeda(textoPreco))
                        }) { Text("OK") }
                    } else {
                        Text(
                            formatarMoeda(produto.precoCarrinho),
                            style = MaterialTheme.typography.bodyMedium,
                            color = SupermercadoColorTokens.onSurfaceMuted,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                        TextButton(onClick = { editandoPreco = true }) { Text("Editar preço") }
                    }
                }
                Text(
                    formatarMoeda(produto.precoCarrinho * produto.qtdCarrinho),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = { onQtdChange(-1) }, modifier = Modifier.height(36.dp)) {
                    Icon(Icons.Filled.Remove, contentDescription = "Diminuir")
                }
                Text(
                    produto.qtdCarrinho.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
                OutlinedButton(onClick = { onQtdChange(1) }, modifier = Modifier.height(36.dp)) {
                    Icon(Icons.Filled.Add, contentDescription = "Aumentar")
                }
            }
        }
    }
}
