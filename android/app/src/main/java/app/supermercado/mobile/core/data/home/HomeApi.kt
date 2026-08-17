package app.supermercado.mobile.core.data.home

import retrofit2.http.GET

/** Espelha GET /api/home (app/routes/api.py), composição feita em
 * ProdutoService.montar_home — mesma regra de negócio do home.html. */
interface HomeApi {
    @GET("api/home")
    suspend fun buscarHome(): HomeResponseDto
}
