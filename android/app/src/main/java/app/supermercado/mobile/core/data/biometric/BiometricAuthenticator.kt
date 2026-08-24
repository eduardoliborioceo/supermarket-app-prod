package app.supermercado.mobile.core.data.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.suspendCancellableCoroutine

private const val AUTENTICADORES = BIOMETRIC_STRONG or BIOMETRIC_WEAK

/**
 * Encapsula BiometricPrompt/BiometricManager para o atalho pós-login: a
 * sessão (refresh token) já fica salva localmente — a digital/rosto é só a
 * trava de acesso por cima dela, nunca substitui o login com Google em si.
 */
@Singleton
class BiometricAuthenticator @Inject constructor() {

    fun disponivel(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(AUTENTICADORES) == BiometricManager.BIOMETRIC_SUCCESS

    suspend fun autenticar(activity: FragmentActivity, titulo: String): Boolean = suspendCancellableCoroutine { cont ->
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    if (cont.isActive) cont.resumeWith(Result.success(true))
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    if (cont.isActive) cont.resumeWith(Result.success(false))
                }

                override fun onAuthenticationFailed() {
                    // Uma tentativa falhou (digital não reconhecida) — o prompt continua
                    // aberto pro usuário tentar de novo, não resolve a coroutine ainda.
                }
            },
        )

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(titulo)
            .setSubtitle("Use sua digital ou rosto para entrar")
            .setNegativeButtonText("Entrar com Google")
            .setAllowedAuthenticators(AUTENTICADORES)
            .build()

        prompt.authenticate(info)

        cont.invokeOnCancellation { prompt.cancelAuthentication() }
    }
}
