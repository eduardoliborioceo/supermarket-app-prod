package app.supermercado.mobile.core.data.carrinho

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CarrinhoRepository @Inject constructor(private val carrinhoApi: CarrinhoApi) {
    suspend fun atualizarItem(produtoId: Int, quantidade: Double, preco: Double) {
        carrinhoApi.atualizar(AtualizarCarrinhoRequestDto(produtoId, quantidade, preco))
    }

    suspend fun limpar() = carrinhoApi.limpar()

    suspend fun salvarGastoPrevisto(valor: Double) {
        carrinhoApi.salvarGastoPrevisto(GastoPrevistoRequestDto(valor))
    }
}
