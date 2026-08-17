package app.supermercado.mobile.core.data.produtos

import kotlinx.serialization.Serializable

@Serializable
data class AdicionarProdutoRequestDto(
    val nome: String,
    val preco: Double,
    val setor: String,
)

@Serializable
data class AdicionarProdutoResponseDto(
    val status: String,
    val id: Int? = null,
    val nome: String? = null,
    val preco: Double? = null,
    val setor: String? = null,
    val message: String? = null,
)
