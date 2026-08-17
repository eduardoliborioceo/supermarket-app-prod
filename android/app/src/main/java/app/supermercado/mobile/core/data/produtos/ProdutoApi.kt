package app.supermercado.mobile.core.data.produtos

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ProdutoApi {
    @GET("api/produtos")
    suspend fun listar(): ListaProdutosResponseDto

    @POST("api/produto/adicionar")
    suspend fun adicionar(@Body body: AdicionarProdutoRequestDto): Response<AdicionarProdutoResponseDto>

    @POST("api/produto/atualizar")
    suspend fun atualizar(@Body body: AtualizarProdutoRequestDto): Response<MensagemResponseDto>

    @POST("api/produto/excluir")
    suspend fun excluir(@Body body: ExcluirProdutoRequestDto): Response<MensagemResponseDto>

    @POST("api/seed-defaults")
    suspend fun restaurarPadrao(): Response<MensagemResponseDto>
}
