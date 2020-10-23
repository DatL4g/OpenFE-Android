package de.datlag.openfe.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface BackupDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBackup(backup: Backup)

    @Delete
    suspend fun deleteBackup(backup: Backup)

    @Query("SELECT * FROM BackupTable ORDER BY timestamp DESC")
    fun getAllBackupsByTimestamp(): LiveData<List<Backup>>
}
