package app.supermercado.mobile.core.data.auth

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface OAuthCallbackEvent {
    data class Success(val code: String) : OAuthCallbackEvent
    data class Error(val mensagem: String) : OAuthCallbackEvent
}

/** Ponte entre o Intent de deep link recebido pela MainActivity (fora do
 * Compose) e o LoginViewModel que precisa reagir a ele. */
@Singleton
class OAuthCallbackBus @Inject constructor() {
    private val _eventos = MutableSharedFlow<OAuthCallbackEvent>(extraBufferCapacity = 1)
    val eventos: SharedFlow<OAuthCallbackEvent> = _eventos.asSharedFlow()

    fun emit(evento: OAuthCallbackEvent) {
        _eventos.tryEmit(evento)
    }
}
