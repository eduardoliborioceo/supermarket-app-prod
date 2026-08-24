package app.supermercado.mobile.ui.screens.mais

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.supermercado.mobile.core.data.auth.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MaisViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _saindo = MutableStateFlow(false)
    val saindo: StateFlow<Boolean> = _saindo.asStateFlow()

    fun sair(onConcluido: () -> Unit) {
        if (_saindo.value) return
        viewModelScope.launch {
            _saindo.value = true
            authRepository.logout()
            _saindo.value = false
            onConcluido()
        }
    }
}
