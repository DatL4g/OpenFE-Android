package de.datlag.openfe.module

import android.content.Context
import android.os.Build
import androidx.datastore.DataStore
import androidx.datastore.createDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.openfe.commons.androidGreaterOr
import de.datlag.openfe.data.SecureUserSerializer
import de.datlag.openfe.data.UserSerializer
import de.datlag.openfe.datastore.UserPreferences
import de.datlag.openfe.provider.Crypto
import io.michaelrocks.paranoid.Obfuscate

@Module
@InstallIn(ApplicationComponent::class)
@Obfuscate
object DataStoreModule {

    @Provides
    fun providesDataStore(
        @ApplicationContext app: Context,
        crypto: Crypto
    ): DataStore<UserPreferences> {
        return if (androidGreaterOr(Build.VERSION_CODES.M)) {
            app.createDataStore(
                "SecureUserDataStore.pb",
                SecureUserSerializer(crypto)
            )
        } else {
            app.createDataStore(
                "UserDataStore.pb",
                UserSerializer()
            )
        }
    }
}
