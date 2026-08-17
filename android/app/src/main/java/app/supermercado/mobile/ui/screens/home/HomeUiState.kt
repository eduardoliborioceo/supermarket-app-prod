package app.supermercado.mobile.ui.screens.home

data class ProdutoUi(
    val id: Int,
    val nome: String,
    val setor: String,
    val qtdCarrinho: Int,
    val precoCarrinho: Double,
)

data class CategoriaUi(
    val nome: String,
    val produtos: List<ProdutoUi>,
) {
    val total: Double get() = produtos.sumOf { it.precoCarrinho * it.qtdCarrinho }
}

data class HomeUiState(
    val carregando: Boolean = true,
    val erro: String? = null,
    val categorias: List<CategoriaUi> = emptyList(),
    val gastoPrevisto: Double = 0.0,
) {
    val totalAtual: Double get() = categorias.sumOf { it.total }
    val saldoDisponivel: Double get() = gastoPrevisto - totalAtual
}
