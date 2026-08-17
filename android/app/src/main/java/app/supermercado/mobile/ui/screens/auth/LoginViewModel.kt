package app.supermercado.mobile.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.supermercado.mobile.core.data.auth.AuthRepository
import app.supermercado.mobile.core.data.auth.OAuthCallbackBus
import app.supermercado.mobile.core.data.auth.OAuthCallbackEvent
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
    private val oAuthCallbackBus: OAuthCallbackBus,
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    val urlLoginGoogle: String get() = authRepository.urlLoginGoogle()

    init {
        viewModelScope.launch {
            oAuthCallbackBus.eventos.collect { evento ->
                when (evento) {
                    is OAuthCallbackEvent.Success -> trocarCodigo(evento.code)
                    is OAuthCallbackEvent.Error -> _state.value =
                        LoginState.Erro("Não foi possível entrar com o Google. Tente novamente.")
                }
            }
        }
    }

    fun iniciarLogin() {
        _state.value = LoginState.Carregando
    }

    private fun trocarCodigo(code: String) {
        viewModelScope.launch {
            _state.value = LoginState.Carregando
            try {
                authRepository.trocarCodigo(code)
                _state.value = LoginState.Sucesso
            } catch (e: Exception) {
                _state.value = LoginState.Erro("Não foi possível entrar. Verifique sua conexão.")
            }
        }
    }
}
