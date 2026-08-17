package app.supermercado.mobile

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import app.supermercado.mobile.core.data.auth.OAuthCallbackBus
import app.supermercado.mobile.core.data.auth.OAuthCallbackEvent
import app.supermercado.mobile.ui.navigation.SupermercadoNavHost
import app.supermercado.mobile.ui.theme.SupermercadoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Minimum time the branded system splash (logo + "From Vertex Code") stays
 * on screen before handing off to the first real screen, so the brand intro
 * is readable even on fast devices. */
private const val SPLASH_MIN_DISPLAY_DURATION_MS = 1600L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var oAuthCallbackBus: OAuthCallbackBus

    private var isSplashMinDurationElapsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isSplashMinDurationElapsed }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleOAuthCallback(intent)

        lifecycleScope.launch {
            delay(SPLASH_MIN_DISPLAY_DURATION_MS)
            isSplashMinDurationElapsed = true
        }

        setContent {
            SupermercadoTheme {
                SupermercadoNavHost()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOAuthCallback(intent)
    }

    private fun handleOAuthCallback(intent: Intent?) {
        val data = intent?.data ?: return
        if (data.scheme != "app.supermercado.mobile" || data.host != "oauth-callback") return

        val code = data.getQueryParameter("code")
        val error = data.getQueryParameter("error")
        when {
            code != null -> oAuthCallbackBus.emit(OAuthCallbackEvent.Success(code))
            error != null -> oAuthCallbackBus.emit(OAuthCallbackEvent.Error(error))
        }
    }
}
