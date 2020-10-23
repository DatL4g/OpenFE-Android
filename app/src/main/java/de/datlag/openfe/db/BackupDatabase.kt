package de.datlag.openfe.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [Backup::class],
    version = 1
)
abstract class BackupDatabase : RoomDatabase() {

    abstract fun getBackupDao(): BackupDAO
}
