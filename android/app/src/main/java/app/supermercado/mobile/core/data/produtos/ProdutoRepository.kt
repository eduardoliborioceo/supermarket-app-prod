package app.supermercado.mobile.core.data.produtos

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

sealed interface ResultadoAdicionarProduto {
    data class Sucesso(val id: Int, val nome: String, val preco: Double, val setor: String) : ResultadoAdicionarProduto
    data class Erro(val mensagem: String) : ResultadoAdicionarProduto
}

@Singleton
class ProdutoRepository @Inject constructor(
    private val produtoApi: ProdutoApi,
    private val json: Json,
) {
    suspend fun adicionar(nome: String, preco: Double, setor: String): ResultadoAdicionarProduto {
        val resposta = produtoApi.adicionar(AdicionarProdutoRequestDto(nome, preco, setor))

        val corpo = resposta.body()
        if (resposta.isSuccessful && corpo?.id != null) {
            return ResultadoAdicionarProduto.Sucesso(corpo.id, corpo.nome ?: nome, corpo.preco ?: preco, corpo.setor ?: setor)
        }

        val mensagemErro = resposta.errorBody()?.string()?.let {
            runCatching { json.decodeFromString(AdicionarProdutoResponseDto.serializer(), it).message }.getOrNull()
        }
        return ResultadoAdicionarProduto.Erro(mensagemErro ?: "Erro ao salvar produto.")
    }
}
