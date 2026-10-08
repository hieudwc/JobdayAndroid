package vn.jobday.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.jobday.app.BuildConfig
import vn.jobday.core.network.di.NetworkConfig
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {

    @Provides
    @Singleton
    fun provideNetworkConfig(): NetworkConfig {
        return NetworkConfig(
            baseUrl = BuildConfig.API_BASE_URL,
            enableLogging = BuildConfig.DEBUG,
        )
    }
}