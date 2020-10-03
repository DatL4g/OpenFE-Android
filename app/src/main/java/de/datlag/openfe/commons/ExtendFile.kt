package de.datlag.openfe.commons

import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import de.datlag.openfe.data.Usage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

fun File.getRootOfStorage(): String {
    var file = this
    while (true) {
        val parentFile = file.parentFile ?: if (file.parent != null) File(file.parent!!) else null
        if (parentFile == null || parentFile.totalSpace != file.totalSpace) {
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

fun File.getUri(): Uri = Uri.fromFile(this)

fun File.getProviderUri(context: Context): Uri? = FileProvider.getUriForFile(
    context,
    "${context.applicationContext.packageName}.fileprovider",
    this
)

fun File.getMimeType(context: Context): String? {
    if (this.isDirectory) {
        return null
    }

    fun fallbackMimeType(uri: Uri): String? {
        return if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
            context.contentResolver.getType(uri)
        } else {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.getExtension()?.toLower())
        }
    }

    fun catchUrlMimeType(): String? {
        val uri = getUri()

        return if (androidGreaterOr(Build.VERSION_CODES.O)) {
            val path = Paths.get(uri.toString())
            try {
                Files.probeContentType(path) ?: fallbackMimeType(uri)
            } catch (ignored: Exception) {
                fallbackMimeType(uri)
            }
        } else {
            fallbackMimeType(uri)
        }
    }

    return try {
        URLConnection.guessContentTypeFromStream(this.inputStream()) ?: catchUrlMimeType()
    } catch (ignored: Exception) {
        catchUrlMimeType()
    }
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
    return (
        this.name.isBlank() ||
            this.name.isEmpty() ||
            this.name == "0" ||
            this.name == "-" ||
            this.name == "sdcard" ||
            this.name == "sdcard0" ||
            this.name == "emulated" ||
            this.name == "legacy"
        )
}

fun File.isAPK(): Boolean {
    val extension = this.getExtension()
    return if (extension != null && (extension.toLower() == ".apk" || extension.toLower() == "apk")) {
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
                if (zipEntry.name.toLower() == dexFile.toLower()) {
                    hasDex = true
                } else if (zipEntry.name.toLower() == manifestFile.toLower()) {
                    hasManifest = true
                }
                if (hasDex && hasManifest) {
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
    return if (this.isAPK()) {
        val packageInfo = context.packageManager.getPackageArchiveInfo(
            this.absolutePath,
            PackageManager.GET_ACTIVITIES
        )
        if (packageInfo != null) {
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

// Pair(readable, writeable)
fun File.getPermissions(): Pair<Boolean, Boolean> {
    return when (EnvironmentCompat.getStorageState(this)) {
        Environment.MEDIA_MOUNTED -> Pair(first = true, second = true)
        Environment.MEDIA_MOUNTED_READ_ONLY -> Pair(first = true, second = false)
        else -> Pair(first = false, second = false)
    }
}

fun File.copyTo(
    target: File,
    overwrite: Boolean = false,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    listener: ((Float) -> Unit)? = null
): File {
    if (!this.exists()) {
        throw NoSuchFileException(this, null, "The source file doesn't exist.")
    }

    if (target.exists()) {
        if (!overwrite)
            throw FileAlreadyExistsException(this, target, "The destination file already exists.")
        else if (!target.delete())
            throw FileAlreadyExistsException(
                this,
                target,
                "Tried to overwrite the destination, but failed to delete it."
            )
    }

    if (this.isDirectory) {
        if (!target.mkdirs())
            throw FileSystemException(this, target, "Failed to create target directory.")
    } else {
        target.parentFile?.mkdirs()

        this.inputStream().use { input ->
            target.outputStream().use { output ->
                input.copyTo(output, bufferSize, this.length(), listener)
            }
        }
    }

    return target
}

suspend fun File.deleteRecursively(listener: (Float) -> Unit): Boolean {
    val totalSize = sizeRecursively(FileWalkDirection.BOTTOM_UP)
    var doneSize: Long = 0
    return walkBottomUp().fold(
        true,
        { res, file ->
            doneSize += file.length()
            withContext(Dispatchers.Main) {
                delay(1000)
                listener.invoke(((doneSize * 100) / totalSize).toFloat())
            }
            (file.delete() || !file.exists()) && res
        }
    )
}

fun File.sizeRecursively(direction: FileWalkDirection = FileWalkDirection.TOP_DOWN): Long {
    var size: Long = 0
    walk(direction).fold(
        true,
        { res, file ->
            size += file.length()
            file.exists() && res
        }
    )
    return size
}

fun File.countRecursively(direction: FileWalkDirection = FileWalkDirection.TOP_DOWN): Int {
    var count = 1
    walk(direction).fold(
        true,
        { res, file ->
            count += file.listFiles()?.size ?: 0
            file.exists() && res
        }
    )
    return count
}
