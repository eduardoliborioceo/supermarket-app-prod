package app.supermercado.mobile.ui.screens.autologin

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.supermercado.mobile.core.data.auth.AuthRepository
import app.supermercado.mobile.core.data.biometric.BiometricAuthenticator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AutoLoginState {
    data object Verificando : AutoLoginState
    data object PedirBiometria : AutoLoginState
    data object Sucesso : AutoLoginState
    data object IrParaLogin : AutoLoginState
}

/**
 * Decide, na abertura do app, se dá pra pular a tela de login: existe sessão
 * salva (refresh token) e, se o aparelho suporta biometria, exige digital/
 * rosto como trava antes de continuar — atalho pós-login (task 3), nunca
 * substitui o login com Google em si.
 */
@HiltViewModel
class AutoLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val biometricAuthenticator: BiometricAuthenticator,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _state = MutableStateFlow<AutoLoginState>(AutoLoginState.Verificando)
    val state: StateFlow<AutoLoginState> = _state.asStateFlow()

    init {
        if (!authRepository.estaLogado) {
            _state.value = AutoLoginState.IrParaLogin
        } else if (biometricAuthenticator.disponivel(context)) {
            _state.value = AutoLoginState.PedirBiometria
        } else {
            validarSessao()
        }
    }

    /** [activity] é usada só transitoriamente pra este autenticate() — nunca fica guardada em campo. */
    fun autenticarComBiometria(activity: FragmentActivity) {
        viewModelScope.launch {
            val sucesso = biometricAuthenticator.autenticar(activity, "Entrar no Supermercado")
            if (sucesso) validarSessao() else _state.value = AutoLoginState.IrParaLogin
        }
    }

    private fun validarSessao() {
        viewModelScope.launch {
            _state.value = if (authRepository.renovarSessao()) {
                AutoLoginState.Sucesso
            } else {
                AutoLoginState.IrParaLogin
            }
        }
    }
}
