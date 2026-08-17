package app.supermercado.mobile.core.di

import app.supermercado.mobile.BuildConfig
import app.supermercado.mobile.core.data.auth.AuthApi
import app.supermercado.mobile.core.data.carrinho.CarrinhoApi
import app.supermercado.mobile.core.data.home.HomeApi
import app.supermercado.mobile.core.data.produtos.ProdutoApi
import app.supermercado.mobile.core.network.AuthInterceptor
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
 * `AuthInterceptor` só lê o token salvo (nunca chama `AuthApi`), então não
 * cria dependência circular com este mesmo Retrofit/OkHttpClient que também
 * fornece a `AuthApi`. Renovação reativa em 401 (Authenticator) fica pra
 * quando isso se mostrar necessário na prática — hoje a Home já renova a
 * sessão proativamente no cold start (AutoLoginScreen).
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json { ignoreUnknownKeys = true }

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
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

    @Provides
    @Singleton
    fun provideHomeApi(retrofit: Retrofit): HomeApi = retrofit.create(HomeApi::class.java)

    @Provides
    @Singleton
    fun provideCarrinhoApi(retrofit: Retrofit): CarrinhoApi = retrofit.create(CarrinhoApi::class.java)

    @Provides
    @Singleton
    fun provideProdutoApi(retrofit: Retrofit): ProdutoApi = retrofit.create(ProdutoApi::class.java)
}
