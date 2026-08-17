package app.supermercado.mobile.core.data.produtos

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/** Espelha app/routes/api.py > produto/adicionar. `Response<T>` (em vez do
 * DTO puro) porque o Flask devolve 400 com uma mensagem de validação no
 * corpo — precisamos ler esse corpo também no caminho de erro. */
interface ProdutoApi {
    @POST("api/produto/adicionar")
    suspend fun adicionar(@Body body: AdicionarProdutoRequestDto): Response<AdicionarProdutoResponseDto>
}
