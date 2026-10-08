package vn.jobday.core.datastore.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import vn.jobday.core.datastore.session.DefaultSessionStore
import vn.jobday.core.datastore.session.SessionStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SessionStoreModule {

    @Binds
    @Singleton
    abstract fun bindSessionStore(
        implementation: DefaultSessionStore,
    ): SessionStore
}