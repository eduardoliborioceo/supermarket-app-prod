package app.supermercado.mobile.core.data.carrinho

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AtualizarCarrinhoRequestDto(
    @SerialName("produto_id") val produtoId: Int,
    val quantidade: Double,
    val preco: Double,
)

@Serializable
data class GastoPrevistoRequestDto(val valor: Double)
