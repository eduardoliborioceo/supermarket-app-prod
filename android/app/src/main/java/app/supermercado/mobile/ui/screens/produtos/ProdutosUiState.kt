package app.supermercado.mobile.ui.screens.produtos

data class ProdutoAdminUi(
    val id: Int,
    val nome: String,
    val setor: String,
    val ultimoPreco: Double,
    val imagemUrl: String? = null,
)

data class CategoriaAdminUi(
    val nome: String,
    val produtos: List<ProdutoAdminUi>,
    val expandido: Boolean = false,
)

data class ProdutosUiState(
    val carregando: Boolean = true,
    val erro: String? = null,
    val categorias: List<CategoriaAdminUi> = emptyList(),
    val restaurandoPadrao: Boolean = false,
) {
    val totalProdutos: Int get() = categorias.sumOf { it.produtos.size }
}
