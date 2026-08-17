package app.supermercado.mobile.core.data.produtos

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

sealed interface ResultadoAdicionarProduto {
    data class Sucesso(val id: Int, val nome: String, val preco: Double, val setor: String) : ResultadoAdicionarProduto
    data class Erro(val mensagem: String) : ResultadoAdicionarProduto
}

sealed interface ResultadoOperacao {
    data object Sucesso : ResultadoOperacao
    data class Erro(val mensagem: String) : ResultadoOperacao
}

@Singleton
class ProdutoRepository @Inject constructor(
    private val produtoApi: ProdutoApi,
    private val json: Json,
) {
    suspend fun listar(): ListaProdutosResponseDto = produtoApi.listar()

    suspend fun adicionar(nome: String, preco: Double, setor: String): ResultadoAdicionarProduto {
        val resposta = produtoApi.adicionar(AdicionarProdutoRequestDto(nome, preco, setor))

        val corpo = resposta.body()
        if (resposta.isSuccessful && corpo?.id != null) {
            return ResultadoAdicionarProduto.Sucesso(corpo.id, corpo.nome ?: nome, corpo.preco ?: preco, corpo.setor ?: setor)
        }

        return ResultadoAdicionarProduto.Erro(mensagemDeErro(resposta.errorBody()?.string()) ?: "Erro ao salvar produto.")
    }

    suspend fun atualizar(id: Int, nome: String, preco: Double, setor: String): ResultadoOperacao {
        val resposta = produtoApi.atualizar(AtualizarProdutoRequestDto(id, nome, preco, setor))
        return resultadoDe(resposta.isSuccessful, resposta.errorBody()?.string(), "Erro ao salvar produto.")
    }

    suspend fun excluir(id: Int): ResultadoOperacao {
        val resposta = produtoApi.excluir(ExcluirProdutoRequestDto(id))
        return resultadoDe(resposta.isSuccessful, resposta.errorBody()?.string(), "Erro ao excluir produto.")
    }

    suspend fun restaurarPadrao(): ResultadoOperacao {
        val resposta = produtoApi.restaurarPadrao()
        return resultadoDe(resposta.isSuccessful, resposta.errorBody()?.string(), "Erro ao restaurar lista padrão.")
    }

    private fun resultadoDe(sucesso: Boolean, corpoErro: String?, mensagemPadrao: String): ResultadoOperacao {
        if (sucesso) return ResultadoOperacao.Sucesso
        return ResultadoOperacao.Erro(mensagemDeErro(corpoErro) ?: mensagemPadrao)
    }

    private fun mensagemDeErro(corpoErro: String?): String? {
        corpoErro ?: return null
        return runCatching { json.decodeFromString(MensagemResponseDto.serializer(), corpoErro).message }.getOrNull()
    }
}
