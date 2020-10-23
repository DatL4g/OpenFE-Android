package de.datlag.openfe.module

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import de.datlag.openfe.R
import de.datlag.openfe.db.BackupDatabase
import io.michaelrocks.paranoid.Obfuscate
import javax.inject.Singleton

@Module
@InstallIn(ApplicationComponent::class)
@Obfuscate
object AppModule {

    @Singleton
    @Provides
    fun providePackageManager(app: Application): PackageManager = app.applicationContext.packageManager

    @Singleton
    @Provides
    fun provideBackupDatabase(
        @ApplicationContext app: Context
    ) = Room.databaseBuilder(
        app,
        BackupDatabase::class.java,
        app.getString(R.string.backup_database)
    ).build()

    @Singleton
    @Provides
    fun provideBackupDao(db: BackupDatabase) = db.getBackupDao()
}
