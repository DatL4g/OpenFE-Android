package de.datlag.openfe.repository

import de.datlag.openfe.db.Backup
import de.datlag.openfe.db.BackupDAO
import javax.inject.Inject

class BackupRepository @Inject constructor(
    val backupDao: BackupDAO
) {
    suspend fun insertBackup(backup: Backup) = backupDao.insertBackup(backup)

    suspend fun deleteBackup(backup: Backup) = backupDao.deleteBackup(backup)

    fun getAllBackupsByTimestamp() = backupDao.getAllBackupsByTimestamp()
}
