package app.supermercado.mobile.core.network

import app.supermercado.mobile.core.data.session.SessionManager
import javax.inject.Inject
import okhttp3.Interceptor
import okhttp3.Response

/** Injeta `Authorization: Bearer` nas chamadas autenticadas. Os endpoints de
 * login/refresh (AuthApi) não precisam dele, mas recebê-lo sem token salvo
 * (header ausente) é inofensivo — o Flask só olha esse header quando não há
 * sessão de cookie (ver app/routes/auth.py > login_required). */
class AuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val accessToken = sessionManager.accessToken ?: return chain.proceed(original)

        val autenticada = original.newBuilder()
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(autenticada)
    }
}
