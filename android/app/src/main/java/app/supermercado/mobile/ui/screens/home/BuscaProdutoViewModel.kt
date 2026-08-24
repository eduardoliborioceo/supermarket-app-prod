package app.supermercado.mobile.ui.screens.home

import app.supermercado.mobile.core.data.produtos.ProdutoAdminDto
import app.supermercado.mobile.core.data.produtos.ProdutoRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BuscaProdutoUiState(
    val carregando: Boolean = true,
    val erro: String? = null,
    val produtos: List<ProdutoAdminDto> = emptyList(),
)

/** Busca em TODO o catalogo cadastrado (nao so o que ja esta na lista de
 * compras de hoje) — quando tem muito produto, achar um pra editar rolando
 * categoria por categoria fica ruim; a busca resolve isso direto. */
@HiltViewModel
class BuscaProdutoViewModel @Inject constructor(
    private val produtoRepository: ProdutoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BuscaProdutoUiState())
    val state: StateFlow<BuscaProdutoUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val produtos = produtoRepository.listar().produtos
                _state.update { it.copy(carregando = false, produtos = produtos) }
            } catch (e: Exception) {
                _state.update { it.copy(carregando = false, erro = "Não foi possível carregar os produtos.") }
            }
        }
    }
}

fun normalizarBusca(texto: String): String {
    val semAcentos = java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
        .replace(Regex("\\p{M}"), "")
    return semAcentos.lowercase()
}
