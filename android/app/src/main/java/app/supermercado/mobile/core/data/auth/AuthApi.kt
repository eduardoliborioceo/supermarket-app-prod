package app.supermercado.mobile.core.data.auth

import retrofit2.http.Body
import retrofit2.http.POST

/** Espelha os endpoints de app/routes/api.py (Fase 2 do plano de migração). */
interface AuthApi {
    @POST("api/auth/google-idtoken")
    suspend fun loginComGoogleIdToken(@Body body: GoogleIdTokenRequestDto): TokenResponseDto

    @POST("api/auth/refresh")
    suspend fun renovar(@Body body: RefreshRequestDto): TokenResponseDto

    @POST("api/auth/logout")
    suspend fun logout(@Body body: RefreshRequestDto)
}
