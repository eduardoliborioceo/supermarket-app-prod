package app.supermercado.mobile.ui.screens.supermercado

import app.supermercado.mobile.core.data.supermercado.ResultadoOperacaoSupermercado
import app.supermercado.mobile.core.data.supermercado.SupermercadoRepository
import app.supermercado.mobile.core.data.supermercado.SupermercadoResultadoDto
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SelecionarSupermercadoViewModel @Inject constructor(
    private val supermercadoRepository: SupermercadoRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SelecionarSupermercadoUiState())
    val state: StateFlow<SelecionarSupermercadoUiState> = _state.asStateFlow()

    private var buscaJob: Job? = null

    init {
        carregar()
    }

    fun carregar() {
        viewModelScope.launch {
            _state.update { it.copy(carregando = true, erro = null) }
            try {
                val dados = supermercadoRepository.atual()
                _state.update {
                    it.copy(
                        carregando = false,
                        hasApi = dados.hasApi,
                        atualNome = dados.nome,
                        atualEndereco = dados.endereco,
                        atualPlaceId = dados.placeId ?: "",
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(carregando = false, erro = "Não foi possível carregar. Verifique sua conexão.") }
            }
        }
    }

    fun alterarQuery(query: String) {
        _state.update { it.copy(query = query) }
        buscaJob?.cancel()
        if (query.trim().length < 2) return
        buscaJob = viewModelScope.launch {
            delay(450)
            buscarPorTexto(query.trim())
        }
    }

    private suspend fun buscarPorTexto(query: String) {
        _state.update { it.copy(buscando = true, hint = "Buscando...") }
        try {
            aplicarResultados(supermercadoRepository.buscar(query = query))
        } catch (e: Exception) {
            _state.update { it.copy(buscando = false, hint = "Erro ao buscar. Verifique sua conexão.") }
        }
    }

    fun iniciarBuscaLocalizacao() {
        buscaJob?.cancel()
        _state.update { it.copy(obtendoLocalizacao = true, hint = "Obtendo localização...") }
    }

    fun buscarPorLocalizacao(lat: Double, lng: Double) {
        viewModelScope.launch {
            _state.update { it.copy(obtendoLocalizacao = false, buscando = true, hint = "Buscando perto de você...") }
            try {
                aplicarResultados(supermercadoRepository.buscar(lat = lat, lng = lng))
            } catch (e: Exception) {
                _state.update { it.copy(buscando = false, hint = "Erro ao buscar. Verifique sua conexão.") }
            }
        }
    }

    fun erroLocalizacao(mensagem: String) {
        _state.update { it.copy(obtendoLocalizacao = false, buscando = false, hint = mensagem) }
    }

    private fun aplicarResultados(resultados: List<SupermercadoResultadoDto>) {
        _state.update {
            it.copy(
                buscando = false,
                buscaFeita = true,
                resultados = resultados.map { r -> SupermercadoResultadoUi(r.placeId, r.nome, r.endereco, r.rating, r.aberto) },
                hint = if (resultados.isEmpty()) "Nenhum supermercado encontrado. Tente outro nome ou cidade." else null,
            )
        }
    }

    fun selecionar(nome: String, endereco: String, placeId: String, onResultado: (ResultadoOperacaoSupermercado) -> Unit) {
        if (nome.isBlank()) {
            onResultado(ResultadoOperacaoSupermercado.Erro("Informe o nome do supermercado."))
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(salvando = true) }
            val resultado = supermercadoRepository.selecionar(nome, endereco, placeId)
            _state.update { it.copy(salvando = false) }
            onResultado(resultado)
        }
    }
}
