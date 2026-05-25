package com.pg.management.di

import com.pg.management.BuildConfig
import com.pg.management.data.auth.remote.AuthApi
import com.pg.management.data.common.AuthInterceptor
import com.pg.management.data.common.TokenAuthenticator
import com.pg.management.data.billing.remote.BillingApi
import com.pg.management.data.complaint.remote.ComplaintApi
import com.pg.management.data.notification.remote.NotificationApi
import com.pg.management.data.room.remote.RoomApi
import com.pg.management.data.tenant.remote.TenantApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun provideHttpClient(
        auth: AuthInterceptor,
        authenticator: TokenAuthenticator,
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        return OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(auth)
            .addInterceptor(logging)
            .authenticator(authenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideTenantApi(retrofit: Retrofit): TenantApi = retrofit.create(TenantApi::class.java)

    @Provides
    @Singleton
    fun provideRoomApi(retrofit: Retrofit): RoomApi = retrofit.create(RoomApi::class.java)

    @Provides
    @Singleton
    fun provideBillingApi(retrofit: Retrofit): BillingApi = retrofit.create(BillingApi::class.java)

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi = retrofit.create(NotificationApi::class.java)

    @Provides
    @Singleton
    fun provideComplaintApi(retrofit: Retrofit): ComplaintApi = retrofit.create(ComplaintApi::class.java)
}
