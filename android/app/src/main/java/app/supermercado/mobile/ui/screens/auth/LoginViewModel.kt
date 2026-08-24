package app.supermercado.mobile.ui.screens.auth

import android.content.Context
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.supermercado.mobile.core.data.auth.AuthRepository
import app.supermercado.mobile.core.data.auth.requestGoogleIdToken
import app.supermercado.mobile.core.data.biometric.BiometricAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface LoginState {
    data object Idle : LoginState
    data object Carregando : LoginState
    data object Sucesso : LoginState
    data class Erro(val mensagem: String) : LoginState
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val biometricAuthenticator: BiometricAuthenticator,
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    /** Sessão salva localmente (mesmo que o usuário tenha cancelado a biometria automática do AutoLoginScreen). */
    val temSessaoSalva: Boolean get() = authRepository.estaLogado

    fun biometriaDisponivel(context: Context): Boolean = biometricAuthenticator.disponivel(context)

    /** Atalho pós-login: a digital/rosto só destrava a sessão já salva
     * (refresh token), nunca substitui o login com Google em si. */
    fun entrarComBiometria(activity: FragmentActivity) {
        viewModelScope.launch {
            _state.value = LoginState.Carregando
            val sucesso = biometricAuthenticator.autenticar(activity, "Entrar no Supermercado")
            if (!sucesso) {
                _state.value = LoginState.Idle
                return@launch
            }
            val renovou = authRepository.renovarSessao()
            _state.value = if (renovou) {
                LoginState.Sucesso
            } else {
                LoginState.Erro("Sua sessão expirou. Entre novamente com o Google.")
            }
        }
    }

    fun loginComGoogle(context: Context) {
        viewModelScope.launch {
            _state.value = LoginState.Carregando
            val idToken = requestGoogleIdToken(context)
                .getOrElse {
                    _state.value = if (it is GetCredentialCancellationException) {
                        LoginState.Idle
                    } else {
                        LoginState.Erro(it.toGoogleSignInMessage())
                    }
                    return@launch
                }
            try {
                authRepository.loginComGoogleIdToken(idToken)
                _state.value = LoginState.Sucesso
            } catch (e: Exception) {
                _state.value = LoginState.Erro("Não foi possível entrar. Tente novamente.")
            }
        }
    }

    private fun Throwable.toGoogleSignInMessage(): String = when (this) {
        is NoCredentialException -> "Nenhuma conta Google encontrada neste aparelho."
        else -> "Não foi possível entrar com o Google. Tente novamente."
    }
}
