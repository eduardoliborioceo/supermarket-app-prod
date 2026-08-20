package app.supermercado.mobile.ui.screens.supermercado

data class SupermercadoResultadoUi(
    val placeId: String,
    val nome: String,
    val endereco: String,
    val rating: Double?,
    val aberto: Boolean?,
)

data class SelecionarSupermercadoUiState(
    val carregando: Boolean = true,
    val erro: String? = null,
    val hasApi: Boolean = true,
    val atualNome: String? = null,
    val atualEndereco: String? = null,
    val atualPlaceId: String = "",
    val query: String = "",
    val buscando: Boolean = false,
    val obtendoLocalizacao: Boolean = false,
    val buscaFeita: Boolean = false,
    val resultados: List<SupermercadoResultadoUi> = emptyList(),
    val hint: String? = null,
    val salvando: Boolean = false,
)
