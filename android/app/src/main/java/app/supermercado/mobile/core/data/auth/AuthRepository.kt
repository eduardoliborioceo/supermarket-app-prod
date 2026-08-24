package app.supermercado.mobile.core.data.auth

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

    suspend fun loginComGoogleIdToken(idToken: String) {
        val tokens = authApi.loginComGoogleIdToken(GoogleIdTokenRequestDto(idToken))
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
