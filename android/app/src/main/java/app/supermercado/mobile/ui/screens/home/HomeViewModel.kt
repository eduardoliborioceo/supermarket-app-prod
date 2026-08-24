package app.supermercado.mobile.ui.screens.home

import app.supermercado.mobile.core.data.auth.AuthRepository
import app.supermercado.mobile.core.data.carrinho.CarrinhoRepository
import app.supermercado.mobile.core.data.home.HomeRepository
import app.supermercado.mobile.core.data.home.HomeResponseDto
import app.supermercado.mobile.core.data.produtos.ProdutoRepository
import app.supermercado.mobile.core.data.produtos.ResultadoAdicionarProduto
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.math.max
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

private const val DEBOUNCE_SALVAR_MS = 500L

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository,
    private val carrinhoRepository: CarrinhoRepository,
    private val produtoRepository: ProdutoRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val jobsCarrinho = mutableMapOf<Int, Job>()
    private var jobGastoPrevisto: Job? = null

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _state.update { it.copy(carregando = true, erro = null) }
            try {
                val dados = buscarHomeComRenovacao()
                _state.update { it.copy(carregando = false, erro = null, categorias = agrupar(dados), gastoPrevisto = dados.gastoPrevisto) }
            } catch (e: Exception) {
                _state.update { it.copy(carregando = false, erro = "Não foi possível carregar sua lista. Verifique sua conexão.") }
            }
        }
    }

    private suspend fun buscarHomeComRenovacao(): HomeResponseDto = try {
        homeRepository.buscarHome()
    } catch (e: HttpException) {
        if (e.code() == 401 && authRepository.renovarSessao()) {
            homeRepository.buscarHome()
        } else {
            throw e
        }
    }

    private fun agrupar(dados: HomeResponseDto): List<CategoriaUi> {
        val porSetor = dados.produtos.groupBy { it.setor }
        return dados.categorias.map { categoria ->
            CategoriaUi(
                nome = categoria,
                produtos = (porSetor[categoria] ?: emptyList()).map {
                    ProdutoUi(it.id, it.nome, it.setor, it.qtdCarrinho, it.precoCarrinho, it.imagemUrl)
                },
            )
        }
    }

    fun alterarQuantidade(produtoId: Int, delta: Int) {
        atualizarProdutoLocal(produtoId) { qtd, preco -> (max(0, qtd + delta)) to preco }
    }

    fun alterarPreco(produtoId: Int, novoPreco: Double) {
        atualizarProdutoLocal(produtoId) { qtd, _ -> qtd to novoPreco }
    }

    private fun atualizarProdutoLocal(produtoId: Int, transformar: (Int, Double) -> Pair<Int, Double>) {
        var novaQtd = 0
        var novoPreco = 0.0
        _state.update { estadoAtual ->
            val categorias = estadoAtual.categorias.map { categoria ->
                categoria.copy(
                    produtos = categoria.produtos.map { produto ->
                        if (produto.id == produtoId) {
                            val (qtd, preco) = transformar(produto.qtdCarrinho, produto.precoCarrinho)
                            novaQtd = qtd
                            novoPreco = preco
                            produto.copy(qtdCarrinho = qtd, precoCarrinho = preco)
                        } else {
                            produto
                        }
                    },
                )
            }
            estadoAtual.copy(categorias = categorias)
        }
        agendarSalvarItem(produtoId, novaQtd, novoPreco)
    }

    private fun agendarSalvarItem(produtoId: Int, quantidade: Int, preco: Double) {
        jobsCarrinho[produtoId]?.cancel()
        jobsCarrinho[produtoId] = viewModelScope.launch {
            delay(DEBOUNCE_SALVAR_MS)
            try {
                carrinhoRepository.atualizarItem(produtoId, quantidade.toDouble(), preco)
            } catch (e: Exception) {
                // Falha silenciosa: o valor local já reflete a intenção do
                // usuário; a próxima edição tenta salvar de novo.
            }
        }
    }

    fun alterarGastoPrevisto(valor: Double) {
        _state.update { it.copy(gastoPrevisto = valor) }
        jobGastoPrevisto?.cancel()
        jobGastoPrevisto = viewModelScope.launch {
            delay(DEBOUNCE_SALVAR_MS)
            try {
                carrinhoRepository.salvarGastoPrevisto(valor)
            } catch (e: Exception) {
                // idem: tentativa silenciosa, próxima edição reenvia.
            }
        }
    }

    fun limparCarrinho(onErro: (String) -> Unit) {
        viewModelScope.launch {
            try {
                carrinhoRepository.limpar()
                _state.update { estadoAtual ->
                    estadoAtual.copy(
                        categorias = estadoAtual.categorias.map { categoria ->
                            categoria.copy(produtos = categoria.produtos.map { it.copy(qtdCarrinho = 0) })
                        },
                    )
                }
            } catch (e: Exception) {
                onErro("Erro ao limpar os dados. Tente novamente.")
            }
        }
    }

    fun adicionarProduto(nome: String, preco: Double, setor: String, onResultado: (ResultadoAdicionarProduto) -> Unit) {
        viewModelScope.launch {
            val resultado = produtoRepository.adicionar(nome, preco, setor)
            if (resultado is ResultadoAdicionarProduto.Sucesso) {
                _state.update { estadoAtual ->
                    val categorias = estadoAtual.categorias.map { categoria ->
                        if (categoria.nome == resultado.setor) {
                            categoria.copy(
                                produtos = categoria.produtos + ProdutoUi(resultado.id, resultado.nome, resultado.setor, 0, resultado.preco),
                            )
                        } else {
                            categoria
                        }
                    }
                    estadoAtual.copy(categorias = categorias)
                }
            }
            onResultado(resultado)
        }
    }
}
