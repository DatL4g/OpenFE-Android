package de.datlag.openfe.commons

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import de.datlag.openfe.data.Usage
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun File.getRootOfStorage(): String {
    var file = this
    while(true) {
        val parentFile = file.parentFile
        if(parentFile == null || parentFile.totalSpace != file.totalSpace) {
            return file.absolutePath
        }
        file = parentFile
    }
}

fun File.getUsage(): Usage {
    val used = this.totalSpace - this.freeSpace
    return Usage(
        this,
        this.totalSpace,
        used,
        (used.toDouble() / this.totalSpace.toDouble() * 100).toFloat()
    )
}

fun File.getExtension(): String? = MimeTypeMap.getFileExtensionFromUrl(getUri().toString())

fun File.getUri(): Uri? = Uri.fromFile(this)

fun File.getProviderUri(context: Context): Uri? = FileProvider.getUriForFile(context, "${context.applicationContext.packageName}.fileprovider", this)

fun File.getMime(context: Context): String? {
    if(this.isDirectory) {
        return null
    }

    var type: String? = null
    getExtension()?.let { type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(it.toLower()) }

    if(type == null) {
        getUri()?.let {
            if(it.scheme == ContentResolver.SCHEME_CONTENT) {
                type = context.contentResolver.getType(it)
            }
        }
    }

    return type
}

fun File.getDisplayName(context: Context): String {
    val rootFile = File(this.getRootOfStorage())

    return if (this == context.filesDir) {
        "App Storage"
    } else {
        if (rootFile.isInternal()) {
            "Internal Storage"
        } else {
            rootFile.name
        }
    }
}

fun File.isInternal(): Boolean {
    return (this.name.isBlank()
            || this.name.isEmpty()
            || this.name == "0"
            || this.name == "-"
            || this.name == "sdcard"
            || this.name == "sdcard0"
            || this.name == "emulated"
            || this.name == "legacy")
}

fun File.isAPK(): Boolean {
    val extension = this.getExtension()
    return if(extension != null && (extension.toLower() == ".apk" || extension.toLower() == "apk")) {
        true
    } else {
        val fileInputStream: FileInputStream?
        val zipInputStream: ZipInputStream?
        var zipEntry: ZipEntry?
        val dexFile = "classes.dex"
        val manifestFile = "AndroidManifest.xml"
        var hasDex = false
        var hasManifest = false

        return try {
            fileInputStream = FileInputStream(this)
            zipInputStream = ZipInputStream(BufferedInputStream(fileInputStream))

            while (zipInputStream.nextEntry != null) {
                zipEntry = zipInputStream.nextEntry
                if(zipEntry.name.toLower() == dexFile.toLower()) {
                    hasDex = true
                } else if(zipEntry.name.toLower() == manifestFile.toLower()) {
                    hasManifest = true
                }
                if(hasDex && hasManifest) {
                    zipInputStream.close()
                    fileInputStream.close()
                    return true
                }
            }
            zipInputStream.close()
            fileInputStream.close()
            false
        } catch (ignored: Exception) {
            false
        }
    }
}

fun File.getAPKImage(context: Context): Drawable? {
    return if(this.isAPK()) {
        val packageInfo = context.packageManager.getPackageArchiveInfo(this.absolutePath, PackageManager.GET_ACTIVITIES)
        if(packageInfo != null) {
            val appInfo = packageInfo.applicationInfo
            appInfo.sourceDir = this.path
            appInfo.publicSourceDir = this.path
            appInfo.loadIcon(context.packageManager)
        } else {
            null
        }
    } else {
        null
    }
}