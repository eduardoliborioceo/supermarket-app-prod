package app.supermercado.mobile.core.data.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import app.supermercado.mobile.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/**
 * Login nativo com Google via Credential Manager — abre a folha de seleção de
 * conta do próprio Android (modal do sistema, sem navegador), substituindo o
 * fluxo antigo de Custom Tab + troca de código.
 *
 * `setServerClientId` usa o mesmo client OAuth Web que o Flask/Authlib já usa
 * (GOOGLE_CLIENT_ID) — é esse valor que o backend valida como audience do ID
 * token em POST /api/auth/google-idtoken.
 */
suspend fun requestGoogleIdToken(context: Context): Result<String> = runCatching {
    val option = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(option)
        .build()

    val response = CredentialManager.create(context).getCredential(context, request)
    val credential = GoogleIdTokenCredential.createFrom(response.credential.data)
    credential.idToken
}
