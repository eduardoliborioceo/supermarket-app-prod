package app.supermercado.mobile.core.data.auth

import app.supermercado.mobile.BuildConfig
import app.supermercado.mobile.core.data.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager,
) {
    val estaLogado: Boolean
        get() = sessionManager.estaLogado

    /** URL aberta na Custom Tab — `client=android` sinaliza o backend a
     * devolver um deep link com código de troca em vez do redirect web. */
    fun urlLoginGoogle(): String = "${BuildConfig.API_BASE_URL}auth/google?client=android"

    suspend fun trocarCodigo(code: String) {
        val tokens = authApi.trocarCodigo(TrocarCodigoRequestDto(code))
        sessionManager.salvarTokens(tokens)
    }

    /** Renovação silenciosa ao reabrir o app — refresh token válido não deve
     * pedir login de novo (ver plano de migração > Autenticação e sessão). */
    suspend fun renovarSessao(): Boolean {
        val refreshToken = sessionManager.refreshToken ?: return false
        return try {
            val tokens = authApi.renovar(RefreshRequestDto(refreshToken))
            sessionManager.salvarTokens(tokens)
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun logout() {
        sessionManager.refreshToken?.let { refreshToken ->
            try {
                authApi.logout(RefreshRequestDto(refreshToken))
            } catch (e: Exception) {
                // Sessão local é limpa de qualquer forma; o refresh token
                // expira sozinho no backend mesmo se essa chamada falhar offline.
            }
        }
        sessionManager.limpar()
    }
}
