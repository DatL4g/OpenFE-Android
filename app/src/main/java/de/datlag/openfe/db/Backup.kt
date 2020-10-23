package de.datlag.openfe.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "BackupTable")
data class Backup(
    var originalDirectoryPath: String = String(),
    var directoryPath: String = String(),
    var timestamp: Long = 0L
) {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null
}
