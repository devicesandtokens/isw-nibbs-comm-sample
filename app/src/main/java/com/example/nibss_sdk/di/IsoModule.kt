package com.example.nibss_sdk.di

import android.content.Context
import com.example.models.NibssConfig
import com.example.nibss_sdk.IsoServiceFactoryWrapper
import com.example.nibss_sdk.KeyValueStoreImpl
import com.interswitchng.smartpos.shared.interfaces.library.KeyValueStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class IsoModule {

    @Provides
    @Singleton
    fun provideNibssConfig(): NibssConfig = NibssConfig(ip = "196.6.103.10", port = 55533)

    @Provides
    @Singleton
    fun provideKeyValueStore(@ApplicationContext context: Context): KeyValueStore =
        KeyValueStoreImpl()

    // Provide factory instead of IsoService directly
    @JvmSuppressWildcards
    @Provides
    @Singleton
    fun provideIsoServiceFactory(
        @ApplicationContext context: Context,
        keyValueStore: KeyValueStore,
        nibssConfig: NibssConfig
    ): IsoServiceFactoryWrapper {
        return IsoServiceFactoryWrapper(context, keyValueStore, nibssConfig)
    }
}
