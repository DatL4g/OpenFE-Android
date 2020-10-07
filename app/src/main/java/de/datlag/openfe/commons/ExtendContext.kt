package de.datlag.openfe.commons

import android.app.usage.StorageStatsManager
import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import androidx.annotation.DimenRes
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import de.datlag.openfe.data.Usage
import java.io.File

fun Context.getStorageVolumes(): Array<Usage> {
    val usageList = mutableListOf<Usage>()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val storageManager = this.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val storageVolumes = storageManager.storageVolumes
        val storageStatsManager = this.getSystemService(Context.STORAGE_STATS_SERVICE) as StorageStatsManager

        for (storageVolume in storageVolumes) {
            var freeSpace: Long
            var totalSpace: Long
            val path = this.getPath(storageVolume)
            val storageFile = if (path != null) File(path) else continue

            if (storageVolume.isPrimary) {
                totalSpace = storageStatsManager.getTotalBytes(StorageManager.UUID_DEFAULT)
                freeSpace = storageStatsManager.getFreeBytes(StorageManager.UUID_DEFAULT)
            } else {
                freeSpace = storageFile.freeSpace
                totalSpace = storageFile.totalSpace
            }

            val usedSpace = totalSpace - freeSpace
            usageList.add(
                Usage(
                    storageFile,
                    totalSpace,
                    usedSpace,
                    (usedSpace.toDouble() / totalSpace.toDouble() * 100).toFloat()
                )
            )
        }
    } else {
        val externalFiles = ContextCompat.getExternalFilesDirs(this, null)

        for (storageFile in externalFiles) {
            usageList.add(storageFile.usage)
        }
    }

    return usageList.toTypedArray()
}

@RequiresApi(Build.VERSION_CODES.N)
fun Context.getPath(storageVolume: StorageVolume): String? {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        storageVolume.directory?.absolutePath?.let { return it }
    }

    try {
        return storageVolume.javaClass.getMethod("getPath").invoke(storageVolume) as String
    } catch (ignored: Exception) { }

    try {
        return (storageVolume.javaClass.getMethod("getPathFile").invoke(storageVolume) as File).absolutePath
    } catch (ignored: Exception) { }

    val extDirs = ContextCompat.getExternalFilesDirs(this, null)
    for (extDir in extDirs) {
        val storageManager = this.getSystemService(Context.STORAGE_SERVICE) as StorageManager
        val fileStorageVolume: StorageVolume = storageManager.getStorageVolume(extDir) ?: continue

        if (fileStorageVolume == storageVolume) {
            var file = extDir
            while (true) {
                val parent = file.parentFile ?: return file.absolutePath
                val parentStorageVolume = storageManager.getStorageVolume(parent) ?: return file.absolutePath
                if (parentStorageVolume != storageVolume) {
                    return file.absolutePath
                }
                file = parent
            }
        }
    }

    try {
        val parcel = Parcel.obtain()
        storageVolume.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        parcel.readString()
        return parcel.readString()
    } catch (ignored: Exception) { }

    return null
}

fun Context.getDimenInPixel(@DimenRes res: Int) = this.resources.getDimensionPixelSize(res)
