package de.datlag.openfe.viewmodel

import android.content.Context
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.datlag.openfe.commons.copyTo
import de.datlag.openfe.commons.updateValue
import de.datlag.openfe.db.Backup
import de.datlag.openfe.repository.BackupRepository
import kotlinx.coroutines.launch
import java.io.File

class BackupViewModel @ViewModelInject constructor(
    val context: Context,
    val backupRepository: BackupRepository
) : ViewModel() {

    private val backupsSortedByTimestamp = backupRepository.getAllBackupsByTimestamp()

    val backups = MediatorLiveData<List<Backup>>()

    init {
        backups.addSource(backupsSortedByTimestamp) { result ->
            result?.let { backups.updateValue(it) }
        }
    }

    fun createBackup(file: File, done: ((Backup) -> Unit)) {
        val dataBackupStorage = File(context.filesDir, "backup")
        if (!dataBackupStorage.exists()) {
            dataBackupStorage.mkdir()
        }

        val fileBackup = File(dataBackupStorage, file.name)
        if (!fileBackup.exists()) {
            fileBackup.createNewFile()
        }

        file.copyTo(fileBackup, true) {
            if (it == 100F) {
                done.invoke(Backup(file.absolutePath, fileBackup.absolutePath, System.currentTimeMillis()))
            }
        }
    }

    fun insertBackup(backup: Backup, done: (() -> Unit)? = null) = viewModelScope.launch {
        backupRepository.insertBackup(backup)
        done?.invoke()
    }
}
