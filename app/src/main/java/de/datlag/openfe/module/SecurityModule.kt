package de.datlag.openfe.module

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import de.datlag.openfe.provider.AesCipherProvider
import de.datlag.openfe.provider.CipherProvider
import de.datlag.openfe.provider.Crypto
import de.datlag.openfe.provider.CryptoImplementation
import io.michaelrocks.paranoid.Obfuscate
import java.security.KeyStore
import javax.inject.Named

@Module(includes = [SecurityModule.Declarations::class])
@InstallIn(ApplicationComponent::class)
@Obfuscate
object SecurityModule {

    const val KEY_NAME = "KeyName"
    const val KEY_STORE_NAME = "KeyStoreName"

    private const val ANDROID_KEY_STORE_TYPE = "AndroidKeyStore"
    private const val DATA_STORE_KEY_NAME = "DataStoreKeyName"

    @Provides
    fun provideKeyStore(): KeyStore =
        KeyStore.getInstance(ANDROID_KEY_STORE_TYPE).apply { load(null) }

    @Provides
    @Named(KEY_NAME)
    fun providesKeyName(): String = DATA_STORE_KEY_NAME

    @Provides
    @Named(KEY_STORE_NAME)
    fun providesKeyStoreName(): String = ANDROID_KEY_STORE_TYPE

    @Module
    @InstallIn(ApplicationComponent::class)
    interface Declarations {

        @Binds
        fun bindsCipherProvider(implementation: AesCipherProvider): CipherProvider

        @Binds
        fun bindsCrypto(implementation: CryptoImplementation): Crypto
    }
}
