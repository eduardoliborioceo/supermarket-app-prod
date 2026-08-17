package app.supermercado.mobile.core.data.produtos

import kotlinx.serialization.SerialName
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

@Serializable
data class ListaProdutosResponseDto(
    val status: String,
    val categorias: List<String>,
    val produtos: List<ProdutoAdminDto>,
)

@Serializable
data class ProdutoAdminDto(
    val id: Int,
    val nome: String,
    val setor: String,
    @SerialName("ultimo_preco") val ultimoPreco: Double,
)

@Serializable
data class AtualizarProdutoRequestDto(
    val id: Int,
    val nome: String,
    val preco: Double,
    val setor: String,
)

@Serializable
data class ExcluirProdutoRequestDto(val id: Int)

@Serializable
data class MensagemResponseDto(val status: String, val message: String? = null)
