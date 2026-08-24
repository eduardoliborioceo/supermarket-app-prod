package app.supermercado.mobile.ui.screens.produtos

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.supermercado.mobile.core.data.produtos.ResultadoOperacao
import app.supermercado.mobile.core.util.formatarMoeda
import app.supermercado.mobile.core.util.parseMoeda
import app.supermercado.mobile.ui.components.PillBadge
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens
import kotlinx.coroutines.delay

private val CardRadius = RoundedCornerShape(16.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProdutosScreen(viewModel: ProdutosViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var mensagem by remember { mutableStateOf<String?>(null) }
    var confirmarExclusaoId by remember { mutableStateOf<Int?>(null) }
    var confirmarRestaurarAberto by remember { mutableStateOf(false) }
    var idRecemSalvo by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(idRecemSalvo) {
        if (idRecemSalvo != null) {
            delay(1000)
            idRecemSalvo = null
        }
    }

    Scaffold(
        containerColor = SupermercadoColorTokens.background,
        topBar = {
            TopAppBar(
                title = { Text("Produtos (${state.totalProdutos})", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SupermercadoColorTokens.surface),
                actions = {
                    IconButton(onClick = { confirmarRestaurarAberto = true }, enabled = !state.restaurandoPadrao) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Restaurar padrão", tint = SupermercadoColorTokens.primary)
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().background(SupermercadoColorTokens.background).padding(innerPadding)) {
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
                else -> Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        TextButton(onClick = viewModel::expandirTodos) { Text("Expandir tudo") }
                        TextButton(onClick = viewModel::recolherTodos) { Text("Recolher tudo") }
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.categorias, key = { it.nome }) { categoria ->
                            CategoriaAdminSection(
                                categoria = categoria,
                                onToggle = { viewModel.alternarExpandido(categoria.nome) },
                                categorias = state.categorias.map { it.nome },
                                idRecemSalvo = idRecemSalvo,
                                onSalvar = { id, nome, preco, setor ->
                                    viewModel.salvarProduto(id, nome, preco, setor) { resultado ->
                                        if (resultado is ResultadoOperacao.Erro) {
                                            mensagem = resultado.mensagem
                                        } else {
                                            idRecemSalvo = id
                                            mensagem = "Produto salvo!"
                                        }
                                    }
                                },
                                onExcluir = { id -> confirmarExclusaoId = id },
                            )
                        }
                    }
                }
            }
        }
    }

    confirmarExclusaoId?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmarExclusaoId = null },
            title = { Text("Excluir produto") },
            text = { Text("Deseja realmente excluir este produto?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarExclusaoId = null
                    viewModel.excluirProduto(id) { resultado ->
                        mensagem = if (resultado is ResultadoOperacao.Erro) resultado.mensagem else "Produto excluído."
                    }
                }) { Text("Excluir", color = SupermercadoColorTokens.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmarExclusaoId = null }) { Text("Cancelar") }
            },
        )
    }

    if (confirmarRestaurarAberto) {
        AlertDialog(
            onDismissRequest = { confirmarRestaurarAberto = false },
            title = { Text("Restaurar padrão") },
            text = { Text("Isso vai adicionar todos os produtos padrão que ainda não existem na sua lista. Continuar?") },
            confirmButton = {
                TextButton(onClick = {
                    confirmarRestaurarAberto = false
                    viewModel.restaurarPadrao { resultado ->
                        mensagem = if (resultado is ResultadoOperacao.Erro) resultado.mensagem else "Lista padrão restaurada."
                    }
                }) { Text("Restaurar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmarRestaurarAberto = false }) { Text("Cancelar") }
            },
        )
    }

    mensagem?.let { texto ->
        AlertDialog(
            onDismissRequest = { mensagem = null },
            confirmButton = { TextButton(onClick = { mensagem = null }) { Text("OK") } },
            text = { Text(texto) },
        )
    }
}

@Composable
private fun CategoriaAdminSection(
    categoria: CategoriaAdminUi,
    categorias: List<String>,
    idRecemSalvo: Int?,
    onToggle: () -> Unit,
    onSalvar: (id: Int, nome: String, preco: Double, setor: String) -> Unit,
    onExcluir: (id: Int) -> Unit,
) {
    Card(
        shape = CardRadius,
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = BorderStroke(1.dp, SupermercadoColorTokens.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.animateContentSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SupermercadoColorTokens.onSurface.copy(alpha = 0.025f))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(categoria.nome, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(10.dp))
                    PillBadge(texto = categoria.produtos.size.toString())
                }
                IconButton(onClick = onToggle) {
                    Icon(
                        if (categoria.expandido) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                        contentDescription = if (categoria.expandido) "Recolher" else "Expandir",
                        tint = SupermercadoColorTokens.onSurfaceMuted,
                    )
                }
            }

            if (categoria.expandido) {
                if (categoria.produtos.isEmpty()) {
                    Text(
                        "Nenhum produto. Use \"Restaurar padrão\" para adicionar produtos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SupermercadoColorTokens.onSurfaceMuted,
                        modifier = Modifier.padding(14.dp),
                    )
                } else {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        categoria.produtos.forEach { produto ->
                            ProdutoAdminCard(
                                produto = produto,
                                categorias = categorias,
                                recemSalvo = produto.id == idRecemSalvo,
                                onSalvar = { nome, preco, setor -> onSalvar(produto.id, nome, preco, setor) },
                                onExcluir = { onExcluir(produto.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProdutoAdminCard(
    produto: ProdutoAdminUi,
    categorias: List<String>,
    recemSalvo: Boolean,
    onSalvar: (nome: String, preco: Double, setor: String) -> Unit,
    onExcluir: () -> Unit,
) {
    var nome by remember(produto.id) { mutableStateOf(produto.nome) }
    var precoTexto by remember(produto.id) { mutableStateOf(formatarMoeda(produto.ultimoPreco).removePrefix("R$ ")) }
    var setor by remember(produto.id) { mutableStateOf(produto.setor) }
    var menuAberto by remember { mutableStateOf(false) }

    val corBorda = if (recemSalvo) SupermercadoColorTokens.success else SupermercadoColorTokens.border
    val larguraBorda = if (recemSalvo) 2.dp else 1.dp

    Card(
        colors = CardDefaults.cardColors(containerColor = SupermercadoColorTokens.surface),
        border = BorderStroke(larguraBorda, corBorda),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = MaterialTheme.shapes.large,
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                PillBadge(texto = "#${produto.id}")
                Text(
                    if (produto.ultimoPreco > 0) formatarMoeda(produto.ultimoPreco) else "sem preço",
                    style = MaterialTheme.typography.labelMedium,
                    color = SupermercadoColorTokens.onSurfaceMuted,
                )
            }

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it },
                label = { Text("Nome") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = precoTexto,
                    onValueChange = { precoTexto = it },
                    label = { Text("Preço (R$)") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.weight(1f),
                )
                ExposedDropdownMenuBox(expanded = menuAberto, onExpandedChange = { menuAberto = it }, modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = setor,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoria") },
                        shape = MaterialTheme.shapes.medium,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = menuAberto) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    DropdownMenu(expanded = menuAberto, onDismissRequest = { menuAberto = false }) {
                        categorias.forEach { opcao ->
                            DropdownMenuItem(text = { Text(opcao) }, onClick = {
                                setor = opcao
                                menuAberto = false
                            })
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { onSalvar(nome.trim(), parseMoeda(precoTexto), setor) },
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(containerColor = SupermercadoColorTokens.primary),
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Salvar")
                }
                OutlinedButton(
                    onClick = onExcluir,
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SupermercadoColorTokens.error),
                    border = BorderStroke(1.dp, SupermercadoColorTokens.error.copy(alpha = 0.35f)),
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Excluir")
                }
            }
        }
    }
}
