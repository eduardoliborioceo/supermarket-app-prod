package app.supermercado.mobile.ui.screens.autologin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.supermercado.mobile.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AutoLoginState {
    data object Verificando : AutoLoginState
    data object Sucesso : AutoLoginState
    data object IrParaLogin : AutoLoginState
}

/** Tela de transição no cold start: refresh token salvo renova a sessão em
 * silêncio, sem pedir login de novo (ver plano de migração > Autenticação). */
@HiltViewModel
class AutoLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AutoLoginState>(AutoLoginState.Verificando)
    val state: StateFlow<AutoLoginState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            if (!authRepository.estaLogado) {
                _state.value = AutoLoginState.IrParaLogin
                return@launch
            }
            _state.value = if (authRepository.renovarSessao()) {
                AutoLoginState.Sucesso
            } else {
                AutoLoginState.IrParaLogin
            }
        }
    }
}
