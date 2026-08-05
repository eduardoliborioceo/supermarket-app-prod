package app.supermercado.mobile.core.network

import app.supermercado.mobile.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Central Retrofit wiring. `BuildConfig.API_BASE_URL` points at the existing
 * Flask JSON API (see docs/mobile-nativo); repositories depend on Retrofit
 * *service interfaces* only, never on this object directly, so swapping the
 * base URL (e.g. to a dedicated mobile-facing service later) is a one-line
 * change here plus new DTOs, not a rewrite of the feature layer.
 *
 * Provided as a plain object for now; once feature modules land this should
 * move into a Hilt @Module (core/di) providing OkHttpClient/Retrofit/services
 * as singletons with the auth token interceptor described in the plan.
 */
object ApiConfig {
    private val json = Json { ignoreUnknownKeys = true }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()
}
