package app.supermercado.mobile.core.data.supermercado

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface SupermercadoApi {
    @GET("api/supermercado/atual")
    suspend fun atual(): SupermercadoAtualResponseDto

    @GET("api/supermercados/buscar")
    suspend fun buscar(
        @Query("q") q: String? = null,
        @Query("lat") lat: Double? = null,
        @Query("lng") lng: Double? = null,
    ): BuscarSupermercadosResponseDto

    @POST("api/supermercado/selecionar")
    suspend fun selecionar(@Body body: SelecionarSupermercadoRequestDto): Response<MensagemResponseDto>
}
