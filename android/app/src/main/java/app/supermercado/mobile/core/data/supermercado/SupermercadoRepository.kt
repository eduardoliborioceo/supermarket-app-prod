package app.supermercado.mobile.core.data.supermercado

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json

sealed interface ResultadoOperacaoSupermercado {
    data object Sucesso : ResultadoOperacaoSupermercado
    data class Erro(val mensagem: String) : ResultadoOperacaoSupermercado
}

@Singleton
class SupermercadoRepository @Inject constructor(
    private val supermercadoApi: SupermercadoApi,
    private val json: Json,
) {
    suspend fun atual(): SupermercadoAtualResponseDto = supermercadoApi.atual()

    suspend fun buscar(query: String? = null, lat: Double? = null, lng: Double? = null): List<SupermercadoResultadoDto> =
        supermercadoApi.buscar(query, lat, lng).results

    suspend fun selecionar(nome: String, endereco: String, placeId: String): ResultadoOperacaoSupermercado {
        val resposta = supermercadoApi.selecionar(SelecionarSupermercadoRequestDto(nome, endereco, placeId))
        if (resposta.isSuccessful) return ResultadoOperacaoSupermercado.Sucesso

        val mensagem = resposta.errorBody()?.string()?.let {
            runCatching { json.decodeFromString(MensagemResponseDto.serializer(), it).message }.getOrNull()
        }
        return ResultadoOperacaoSupermercado.Erro(mensagem ?: "Erro ao salvar. Tente novamente.")
    }
}
