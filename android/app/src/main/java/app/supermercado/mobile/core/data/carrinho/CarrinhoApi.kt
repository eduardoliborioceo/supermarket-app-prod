package app.supermercado.mobile.core.data.carrinho

import retrofit2.http.Body
import retrofit2.http.POST

interface CarrinhoApi {
    @POST("api/carrinho/atualizar")
    suspend fun atualizar(@Body body: AtualizarCarrinhoRequestDto)

    @POST("api/carrinho/limpar")
    suspend fun limpar()

    @POST("api/carrinho/gasto-previsto")
    suspend fun salvarGastoPrevisto(@Body body: GastoPrevistoRequestDto)
}
