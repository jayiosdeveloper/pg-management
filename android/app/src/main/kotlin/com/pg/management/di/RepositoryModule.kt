package com.pg.management.di

import com.pg.management.data.auth.AuthRepositoryImpl
import com.pg.management.data.billing.BillingRepositoryImpl
import com.pg.management.data.complaint.ComplaintRepositoryImpl
import com.pg.management.data.electricity.ElectricityRepositoryImpl
import com.pg.management.data.notification.NotificationRepositoryImpl
import com.pg.management.data.room.RoomRepositoryImpl
import com.pg.management.data.tenant.TenantRepositoryImpl
import com.pg.management.data.worker.WorkerRepositoryImpl
import com.pg.management.domain.auth.AuthRepository
import com.pg.management.domain.repository.BillingRepository
import com.pg.management.domain.repository.ComplaintRepository
import com.pg.management.domain.repository.ElectricityRepository
import com.pg.management.domain.repository.NotificationRepository
import com.pg.management.domain.repository.RoomRepository
import com.pg.management.domain.repository.TenantRepository
import com.pg.management.domain.repository.WorkerRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds @Singleton abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
    @Binds @Singleton abstract fun bindTenantRepository(impl: TenantRepositoryImpl): TenantRepository
    @Binds @Singleton abstract fun bindRoomRepository(impl: RoomRepositoryImpl): RoomRepository
    @Binds @Singleton abstract fun bindBillingRepository(impl: BillingRepositoryImpl): BillingRepository
    @Binds @Singleton abstract fun bindNotificationRepository(impl: NotificationRepositoryImpl): NotificationRepository
    @Binds @Singleton abstract fun bindComplaintRepository(impl: ComplaintRepositoryImpl): ComplaintRepository
    @Binds @Singleton abstract fun bindWorkerRepository(impl: WorkerRepositoryImpl): WorkerRepository
    @Binds @Singleton abstract fun bindElectricityRepository(impl: ElectricityRepositoryImpl): ElectricityRepository
}
