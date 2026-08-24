package app.supermercado.mobile.ui.screens.autologin

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.supermercado.mobile.R
import app.supermercado.mobile.ui.theme.SupermercadoColorTokens

@Composable
fun AutoLoginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AutoLoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val activity = LocalContext.current as FragmentActivity

    LaunchedEffect(state) {
        when (state) {
            is AutoLoginState.PedirBiometria -> viewModel.autenticarComBiometria(activity)
            is AutoLoginState.Sucesso -> onNavigateToHome()
            is AutoLoginState.IrParaLogin -> onNavigateToLogin()
            is AutoLoginState.Verificando -> Unit
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = SupermercadoColorTokens.background) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.ic_supermercado_logo_splash),
                contentDescription = "Supermercado",
                modifier = Modifier.size(72.dp),
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = SupermercadoColorTokens.primary)
        }
    }
}
