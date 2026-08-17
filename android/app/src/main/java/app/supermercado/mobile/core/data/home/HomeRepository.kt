package app.supermercado.mobile.core.data.home

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeRepository @Inject constructor(private val homeApi: HomeApi) {
    suspend fun buscarHome(): HomeResponseDto = homeApi.buscarHome()
}
