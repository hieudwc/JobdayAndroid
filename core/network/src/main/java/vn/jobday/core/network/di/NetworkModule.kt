package vn.jobday.core.network.di
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import vn.jobday.core.network.auth.RefreshTokenApi
import vn.jobday.core.network.auth.api.AuthApi
import vn.jobday.core.network.auth.authenticator.TokenAuthenticator
import vn.jobday.core.network.auth.interceptor.AccessTokenInterceptor
import vn.jobday.core.network.networkJson
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkJson(): Json {
        return networkJson
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(
        networkConfig: NetworkConfig,
    ): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (networkConfig.enableLogging) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    @AuthenticatedNetwork
    fun provideAuthenticatedOkHttpClient(
        accessTokenInterceptor: AccessTokenInterceptor,
        tokenAuthenticator: TokenAuthenticator,
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(accessTokenInterceptor)
            .authenticator(tokenAuthenticator)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @RefreshNetwork
    fun provideRefreshOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    @AuthenticatedNetwork
    fun provideAuthenticatedRetrofit(
        networkConfig: NetworkConfig,
        json: Json,
        @AuthenticatedNetwork okHttpClient: OkHttpClient,
    ): Retrofit {
        return createRetrofit(
            networkConfig = networkConfig,
            json = json,
            okHttpClient = okHttpClient,
        )
    }

    @Provides
    @Singleton
    @RefreshNetwork
    fun provideRefreshRetrofit(
        networkConfig: NetworkConfig,
        json: Json,
        @RefreshNetwork okHttpClient: OkHttpClient,
    ): Retrofit {
        return createRetrofit(
            networkConfig = networkConfig,
            json = json,
            okHttpClient = okHttpClient,
        )
    }

    @Provides
    @Singleton
    fun provideAuthApi(
        @AuthenticatedNetwork retrofit: Retrofit,
    ): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRefreshTokenApi(
        @RefreshNetwork retrofit: Retrofit,
    ): RefreshTokenApi {
        return retrofit.create(RefreshTokenApi::class.java)
    }


    private fun createRetrofit(
        networkConfig: NetworkConfig,
        json: Json,
        okHttpClient: OkHttpClient,
    ): Retrofit {
        val contentType = "application/json".toMediaType()

        return Retrofit.Builder()
            .baseUrl(networkConfig.baseUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }
}