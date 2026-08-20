package app.supermercado.mobile.core.data.supermercado

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SupermercadoAtualResponseDto(
    val status: String,
    @SerialName("has_api") val hasApi: Boolean,
    @SerialName("supermercado_nome") val nome: String? = null,
    @SerialName("supermercado_endereco") val endereco: String? = null,
    @SerialName("supermercado_place_id") val placeId: String? = null,
)

@Serializable
data class BuscarSupermercadosResponseDto(
    val results: List<SupermercadoResultadoDto> = emptyList(),
    @SerialName("google_status") val googleStatus: String? = null,
)

@Serializable
data class SupermercadoResultadoDto(
    @SerialName("place_id") val placeId: String = "",
    val nome: String = "",
    val endereco: String = "",
    val rating: Double? = null,
    val aberto: Boolean? = null,
)

@Serializable
data class SelecionarSupermercadoRequestDto(
    val nome: String,
    val endereco: String,
    @SerialName("place_id") val placeId: String,
)

@Serializable
data class MensagemResponseDto(val status: String, val message: String? = null)
