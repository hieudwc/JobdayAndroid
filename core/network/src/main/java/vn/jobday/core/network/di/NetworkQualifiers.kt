package vn.jobday.core.network.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AuthenticatedNetwork

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class RefreshNetwork