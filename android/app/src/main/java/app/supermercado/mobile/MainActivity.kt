package app.supermercado.mobile

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import app.supermercado.mobile.ui.navigation.SupermercadoNavHost
import app.supermercado.mobile.ui.theme.SupermercadoTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Minimum time the branded system splash (logo + "From Droid Codes") stays
 * on screen before handing off to the first real screen, so the brand intro
 * is readable even on fast devices. */
private const val SPLASH_MIN_DISPLAY_DURATION_MS = 1600L

/** FragmentActivity (não ComponentActivity) porque BiometricPrompt exige esse host. */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private var isSplashMinDurationElapsed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !isSplashMinDurationElapsed }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
}
