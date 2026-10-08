package vn.jobday.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.jobday.core.data.auth.AuthRepository
import vn.jobday.core.data.auth.DefaultAuthRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        implementation: DefaultAuthRepository,
    ): AuthRepository
}