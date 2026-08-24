package app.supermercado.mobile.core.data.home

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HomeResponseDto(
    val status: String,
    val categorias: List<String>,
    @SerialName("gasto_previsto") val gastoPrevisto: Double,
    val produtos: List<ProdutoHomeDto>,
)

@Serializable
data class ProdutoHomeDto(
    val id: Int,
    val nome: String,
    val setor: String,
    @SerialName("qtd_carrinho") val qtdCarrinho: Int,
    @SerialName("preco_carrinho") val precoCarrinho: Double,
    @SerialName("imagem_url") val imagemUrl: String? = null,
)
