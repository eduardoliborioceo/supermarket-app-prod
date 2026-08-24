package app.supermercado.mobile.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.supermercado.mobile.core.data.produtos.ResultadoAdicionarProduto
import app.supermercado.mobile.core.util.formatarMoeda
import app.supermercado.mobile.core.util.parseMoeda
import app.supermercado.mobile.ui.components.PillBadge
import app.supermercado.mobile.ui.components.ProdutoThumbnail
import app.supermercado.mobile.ui.components.QtyStepper
import app.supermercado.mobile.ui.theme.PillShape
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CardRadius = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var dialogAdicionarAberto by rememberSaveable { mutableStateOf(false) }
    var categoriaParaNovoProduto by rememberSaveable { mutableStateOf<String?>(null) }
    var confirmarLimparAberto by rememberSaveable { mutableStateOf(false) }
    var mensagemErroAcao by remember { mutableStateOf<String?>(null) }
    var buscaAberta by rememberSaveable { mutableStateOf(false) }
    var produtoDestacadoId by remember { mutableStateOf<Int?>(null) }
    var catCycleIndex by rememberSaveable { mutableStateOf(0) }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(produtoDestacadoId) {
        if (produtoDestacadoId != null) {
            delay(2500)
            produtoDestacadoId = null
        }
    }

    Scaffold(
        containerColor = SupermercadoColorTokens.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Lista de Compras", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SupermercadoColorTokens.surface),
                windowInsets = WindowInsets(0, 0, 0, 0),
                actions = {
                    IconButton(onClick = { buscaAberta = true }) {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar produto", tint = SupermercadoColorTokens.onSurfaceMuted)
                    }
                    IconButton(onClick = { confirmarLimparAberto = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Limpar carrinho", tint = SupermercadoColorTokens.error)
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    categoriaParaNovoProduto = null
                    dialogAdicionarAberto = true
                },
                containerColor = SupermercadoColorTokens.primary,
                contentColor = Color.White,
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Cadastrar produto")
            }
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().background(SupermercadoColorTokens.background).padding(innerPadding)) {
            when {
                state.carregando -> Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = SupermercadoColorTokens.primary,
                    )
                }
                state.erro != null -> ErroCarregarHome(mensagem = state.erro!!, onTentarNovamente = viewModel::carregar)
                else -> {
                    Surface(
                        color = SupermercadoColorTokens.background,
                        shadowElevation = 3.dp,
                    ) {
                        KpiRow(
                            totalAtual = state.totalAtual,
                            gastoPrevisto = state.gastoPrevisto,
                            saldoDisponivel = state.saldoDisponivel,
                            categorias = state.categorias,
                            catCycleIndex = catCycleIndex,
                            onGastoPrevistoChange = viewModel::alterarGastoPrevisto,
                            onAvancarCategoria = {
                                if (state.categorias.isNotEmpty()) {
                                    catCycleIndex = (catCycleIndex + 1) % state.categorias.size
                                }
                            },
                            onIrParaCategoria = {
                                coroutineScope.launch { listState.animateScrollToItem(catCycleIndex) }
                            },
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        itemsIndexed(state.categorias, key = { _, categoria -> categoria.nome }) { index, categoria ->
                            CategoriaSection(
                                categoria = categoria,
                                corCategoria = SupermercadoColorTokens.categoryColor(index),
                                fundoCategoria = SupermercadoColorTokens.categoryTint(index),
                                produtoDestacadoId = produtoDestacadoId,
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
    }

    if (dialogAdicionarAberto) {
        AdicionarProdutoDialog(
            categorias = state.categorias.map { it.nome },
            categoriaInicial = categoriaParaNovoProduto,
            onDismiss = { dialogAdicionarAberto = false },
            onConfirmar = { nome, preco, setor ->
                viewModel.adicionarProduto(nome, preco, setor) { resultado ->
                    if (resultado is ResultadoAdicionarProduto.Erro) {
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
                }) { Text("Limpar", color = SupermercadoColorTokens.error) }
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

    if (buscaAberta) {
        BuscaProdutoDialog(
            onDismiss = { buscaAberta = false },
            onProdutoSelecionado = { produtoId ->
                buscaAberta = false
                val indiceCategoria = state.categorias.indexOfFirst { categoria -> categoria.produtos.any { it.id == produtoId } }
                if (indiceCategoria != -1) {
                    produtoDestacadoId = produtoId
                    coroutineScope.launch {
                        delay(150)
                        listState.animateScrollToItem(indiceCategoria)
                    }
                }
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

/** Altura compartilhada só pelos 2 cards de cima (Total atual / Gasto
 * previsto) — precisa caber o OutlinedTextField do Gasto previsto (o mais
 * alto dos dois), pra eles nunca ficarem com tamanhos diferentes entre si. O
 * card de Saldo disponível fica de fora: só tem texto, então usa altura
 * própria (wrap content) em vez de herdar essa altura maior à toa. */
private val KpiCardHeight = 92.dp

@Composable
private fun KpiRow(
    totalAtual: Double,
    gastoPrevisto: Double,
    saldoDisponivel: Double,
    categorias: List<CategoriaUi>,
    catCycleIndex: Int,
    onGastoPrevistoChange: (Double) -> Unit,
    onAvancarCategoria: () -> Unit,
    onIrParaCategoria: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var textoGasto by rememberSaveable(gastoPrevisto) {
        mutableStateOf(if (gastoPrevisto == 0.0) "" else formatarMoeda(gastoPrevisto).removePrefix("R$ "))
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
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
                        prefix = { Text("R$") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth(),
                    )
                },
            )
        }

        val saldoNegativo = saldoDisponivel < 0
        val corSaldo = if (saldoNegativo) SupermercadoColorTokens.error else SupermercadoColorTokens.success
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = CardRadius,
            colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
            border = BorderStroke(1.dp, corSaldo.copy(alpha = 0.25f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(corSaldo.copy(alpha = 0.10f), Color.Transparent)))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Saldo disponível", style = MaterialTheme.typography.labelMedium, color = SupermercadoColorTokens.onSurfaceMuted)
                    Text(
                        formatarMoeda(saldoDisponivel),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = corSaldo,
                    )
                }
                if (categorias.isNotEmpty()) {
                    CategoriaCycleBadge(
                        categorias = categorias,
                        index = catCycleIndex,
                        onClick = onAvancarCategoria,
                        onLongClick = onIrParaCategoria,
                    )
                }
            }
        }
    }
}

/** Porta o `.cat-cycle` do PWA (home.html/main.js): mostra o total de uma
 * categoria por vez dentro do card de Saldo disponível. Toque avança pra
 * próxima categoria (só pra consultar o total); toque longo rola a lista até
 * o início dela. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategoriaCycleBadge(
    categorias: List<CategoriaUi>,
    index: Int,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val indiceSeguro = index % categorias.size
    val categoria = categorias[indiceSeguro]
    val cor = SupermercadoColorTokens.categoryColor(indiceSeguro)

    Surface(
        shape = PillShape,
        color = SupermercadoColorTokens.surface,
        border = BorderStroke(1.dp, cor.copy(alpha = 0.4f)),
        modifier = Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(modifier = Modifier.size(8.dp).background(cor, CircleShape))
            Spacer(modifier = Modifier.width(6.dp))
            Column {
                Text(
                    categoria.nome,
                    style = MaterialTheme.typography.labelSmall,
                    color = SupermercadoColorTokens.onSurfaceMuted,
                    maxLines = 1,
                )
                Text(
                    formatarMoeda(categoria.total),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
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
        modifier = modifier.height(KpiCardHeight),
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = BorderStroke(1.dp, SupermercadoColorTokens.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = CardRadius,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(titulo, style = MaterialTheme.typography.labelMedium, color = SupermercadoColorTokens.onSurfaceMuted)
            if (valor != null) {
                Text(valor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
            conteudo?.invoke()
        }
    }
}

@Composable
private fun CategoriaSection(
    categoria: CategoriaUi,
    corCategoria: Color,
    fundoCategoria: Color,
    produtoDestacadoId: Int?,
    onQtdChange: (Int, Int) -> Unit,
    onPrecoChange: (Int, Double) -> Unit,
    onAdicionarClick: () -> Unit,
) {
    Card(
        shape = CardRadius,
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = BorderStroke(1.dp, SupermercadoColorTokens.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(3.dp).background(corCategoria))

            Row(
                modifier = Modifier.fillMaxWidth().background(fundoCategoria).padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(categoria.nome, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = corCategoria)
                    PillBadge(
                        texto = "${categoria.produtos.size} · ${formatarMoeda(categoria.total)}",
                        contentColor = corCategoria,
                        containerColor = fundoCategoria,
                        borderColor = corCategoria.copy(alpha = 0.5f),
                    )
                }
                Surface(
                    onClick = onAdicionarClick,
                    shape = CircleShape,
                    color = fundoCategoria,
                    border = BorderStroke(1.dp, corCategoria),
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = "Adicionar item em ${categoria.nome}",
                        tint = corCategoria,
                        modifier = Modifier.padding(6.dp).size(18.dp),
                    )
                }
            }

            if (categoria.produtos.isEmpty()) {
                Text(
                    "Nenhum item nesta categoria. Use o botão \"+\" para adicionar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SupermercadoColorTokens.onSurfaceMuted,
                    modifier = Modifier.padding(14.dp),
                )
            } else {
                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoria.produtos.forEach { produto ->
                        ProdutoCard(
                            produto = produto,
                            corCategoria = corCategoria,
                            destacado = produto.id == produtoDestacadoId,
                            onQtdChange = { delta -> onQtdChange(produto.id, delta) },
                            onPrecoChange = { novoPreco -> onPrecoChange(produto.id, novoPreco) },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ProdutoCard(
    produto: ProdutoUi,
    corCategoria: Color,
    destacado: Boolean,
    onQtdChange: (Int) -> Unit,
    onPrecoChange: (Double) -> Unit,
) {
    var editandoPreco by remember { mutableStateOf(false) }
    var textoPreco by remember(produto.precoCarrinho) { mutableStateOf(produto.precoCarrinho.toString()) }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    // A rolagem grosseira (listState.animateScrollToItem na Home) só chega no
    // topo da categoria — os produtos não são itens do LazyColumn, ficam
    // dentro do Card da categoria. Esse efeito faz o ajuste fino assim que
    // este card específico entra na composição (a categoria já visível).
    LaunchedEffect(destacado) {
        if (destacado) {
            delay(300)
            bringIntoViewRequester.bringIntoView()
        }
    }

    Card(
        modifier = Modifier.bringIntoViewRequester(bringIntoViewRequester),
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = BorderStroke(if (destacado) 2.dp else 1.dp, if (destacado) SupermercadoColorTokens.primary else SupermercadoColorTokens.border),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.weight(1f)) {
                    ProdutoThumbnail(imagemUrl = produto.imagemUrl)
                    Text(
                        produto.nome,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = corCategoria,
                    )
                }
                Text(
                    formatarMoeda(produto.precoCarrinho * produto.qtdCarrinho),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (editandoPreco) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = textoPreco,
                            onValueChange = { textoPreco = it },
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium,
                            modifier = Modifier.width(110.dp),
                        )
                        TextButton(onClick = {
                            editandoPreco = false
                            onPrecoChange(parseMoeda(textoPreco))
                        }) { Text("OK") }
                    }
                } else {
                    Surface(
                        onClick = { editandoPreco = true },
                        shape = PillShape,
                        color = SupermercadoColorTokens.primary.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, SupermercadoColorTokens.primary.copy(alpha = 0.18f)),
                    ) {
                        Text(
                            formatarMoeda(produto.precoCarrinho),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = SupermercadoColorTokens.primary,
                        )
                    }
                }

                QtyStepper(
                    qtd = produto.qtdCarrinho,
                    onDecrement = { onQtdChange(-1) },
                    onIncrement = { onQtdChange(1) },
                )
            }
        }
    }
}
