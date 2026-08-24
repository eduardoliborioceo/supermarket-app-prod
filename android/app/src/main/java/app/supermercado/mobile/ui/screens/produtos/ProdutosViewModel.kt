package app.supermercado.mobile.ui.screens.produtos

import app.supermercado.mobile.core.data.produtos.ListaProdutosResponseDto
import app.supermercado.mobile.core.data.produtos.ProdutoRepository
import app.supermercado.mobile.core.data.produtos.ResultadoOperacao
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProdutosViewModel @Inject constructor(
    private val produtoRepository: ProdutoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProdutosUiState())
    val state: StateFlow<ProdutosUiState> = _state.asStateFlow()

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _state.update { it.copy(carregando = true, erro = null) }
            try {
                val dados = produtoRepository.listar()
                _state.update { it.copy(carregando = false, categorias = agrupar(dados)) }
            } catch (e: Exception) {
                _state.update { it.copy(carregando = false, erro = "Não foi possível carregar os produtos. Verifique sua conexão.") }
            }
        }
    }

    private fun agrupar(dados: ListaProdutosResponseDto): List<CategoriaAdminUi> {
        val porSetor = dados.produtos.groupBy { it.setor }
        return dados.categorias.map { categoria ->
            val expandidoAntes = _state.value.categorias.find { it.nome == categoria }?.expandido ?: false
            CategoriaAdminUi(
                nome = categoria,
                produtos = (porSetor[categoria] ?: emptyList()).map { ProdutoAdminUi(it.id, it.nome, it.setor, it.ultimoPreco, it.imagemUrl) },
                expandido = expandidoAntes,
            )
        }
    }

    fun alternarExpandido(categoriaNome: String) {
        _state.update { estadoAtual ->
            estadoAtual.copy(
                categorias = estadoAtual.categorias.map {
                    if (it.nome == categoriaNome) it.copy(expandido = !it.expandido) else it
                },
            )
        }
    }

    fun expandirTodos() {
        _state.update { it.copy(categorias = it.categorias.map { c -> c.copy(expandido = true) }) }
    }

    fun recolherTodos() {
        _state.update { it.copy(categorias = it.categorias.map { c -> c.copy(expandido = false) }) }
    }

    fun salvarProduto(id: Int, nome: String, preco: Double, setor: String, onResultado: (ResultadoOperacao) -> Unit) {
        viewModelScope.launch {
            val resultado = produtoRepository.atualizar(id, nome, preco, setor)
            if (resultado is ResultadoOperacao.Sucesso) {
                _state.update { estadoAtual ->
                    estadoAtual.copy(
                        categorias = estadoAtual.categorias.map { categoria ->
                            categoria.copy(
                                produtos = categoria.produtos.map { produto ->
                                    if (produto.id == id) produto.copy(nome = nome, setor = setor, ultimoPreco = preco) else produto
                                },
                            )
                        },
                    )
                }
            }
            onResultado(resultado)
        }
    }

    fun excluirProduto(id: Int, onResultado: (ResultadoOperacao) -> Unit) {
        viewModelScope.launch {
            val resultado = produtoRepository.excluir(id)
            if (resultado is ResultadoOperacao.Sucesso) {
                _state.update { estadoAtual ->
                    estadoAtual.copy(
                        categorias = estadoAtual.categorias.map { categoria ->
                            categoria.copy(produtos = categoria.produtos.filter { it.id != id })
                        },
                    )
                }
            }
            onResultado(resultado)
        }
    }

    fun restaurarPadrao(onResultado: (ResultadoOperacao) -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(restaurandoPadrao = true) }
            val resultado = produtoRepository.restaurarPadrao()
            _state.update { it.copy(restaurandoPadrao = false) }
            if (resultado is ResultadoOperacao.Sucesso) {
                carregar()
            }
            onResultado(resultado)
        }
    }
}
