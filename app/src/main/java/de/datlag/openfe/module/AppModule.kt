package de.datlag.openfe.module

import android.app.Application
import android.content.pm.PackageManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import io.michaelrocks.paranoid.Obfuscate
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
@Obfuscate
object AppModule {

    @Singleton
    @Provides
    fun providePackageManager(app: Application): PackageManager = app.applicationContext.packageManager
}
