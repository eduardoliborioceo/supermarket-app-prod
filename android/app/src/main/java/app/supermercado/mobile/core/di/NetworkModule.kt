package app.supermercado.mobile.core.di

import app.supermercado.mobile.BuildConfig
import app.supermercado.mobile.core.data.auth.AuthApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit/OkHttp central, apontando pro Flask/Railway que já serve o site e
 * o PWA hoje (não há ambiente de desenvolvimento separado — só produção, ver
 * CLAUDE.md). Repositories dependem só das interfaces de API (`AuthApi`
 * etc.), nunca do Retrofit diretamente.
 *
 * `AuthApi` não carrega o `Authorization: Bearer` (os endpoints de
 * login/refresh são o próprio ponto de entrada da sessão), então o
 * interceptor de autenticação entra aqui só quando o primeiro endpoint de
 * negócio autenticado (produtos/carrinho, Fase 3) for adicionado a este
 * módulo — não antecipar essa peça sem um consumidor real ainda.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)
}
