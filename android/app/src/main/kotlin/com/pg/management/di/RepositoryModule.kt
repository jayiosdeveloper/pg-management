package com.pg.management.di

import com.pg.management.data.auth.AuthRepositoryImpl
import com.pg.management.data.room.RoomRepositoryImpl
import com.pg.management.data.tenant.TenantRepositoryImpl
import com.pg.management.domain.auth.AuthRepository
import com.pg.management.domain.repository.RoomRepository
import com.pg.management.domain.repository.TenantRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindTenantRepository(impl: TenantRepositoryImpl): TenantRepository

    @Binds
    @Singleton
    abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository
}
