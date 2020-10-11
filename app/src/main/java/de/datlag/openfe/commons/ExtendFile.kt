package de.datlag.openfe.commons

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import de.datlag.openfe.data.FilePermission
import de.datlag.openfe.data.Usage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.contracts.ExperimentalContracts

fun File.getRootOfStorage(): String {
    var file = this
    while (true) {
        val parentDirectory = file.parentDir
        if (parentDirectory.totalSpace != file.totalSpace) {
            return file.absolutePath
        }
        file = file.parentDir
    }
}

val File.usage: Usage
    get() {
        val used = this.totalSpace - this.freeSpace
        return Usage(
            this,
            this.totalSpace,
            used,
            (used.toDouble() / this.totalSpace.toDouble() * 100).toFloat()
        )
    }

val File.extension: String?
    get() = MimeTypeMap.getFileExtensionFromUrl(this.uri.toString())

val File.uri: Uri
    get() = Uri.fromFile(this)

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
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(this.extension?.toLower())
        }
    }

    fun catchUrlMimeType(): String? {
        val fileUri = this.uri

        return if (androidGreaterOr(Build.VERSION_CODES.O)) {
            val path = Paths.get(fileUri.toString())
            try {
                Files.probeContentType(path) ?: fallbackMimeType(fileUri)
            } catch (ignored: Exception) {
                fallbackMimeType(fileUri)
            }
        } else {
            fallbackMimeType(fileUri)
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

@ExperimentalContracts
fun File.isAPK(): Boolean {
    val fileExtension = this.extension
    return if (fileExtension.isNotCleared() && (fileExtension.toLower() == ".apk" || fileExtension.toLower() == "apk")) {
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

@ExperimentalContracts
@JvmOverloads
fun File.getAPKImage(context: Context, checked: Boolean = false): Drawable? {
    return if (checked || this.isAPK()) {
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

val File.permissions: FilePermission
    get() {
        return when (EnvironmentCompat.getStorageState(this)) {
            Environment.MEDIA_MOUNTED -> FilePermission(readable = true, writeable = true)
            Environment.MEDIA_MOUNTED_READ_ONLY -> FilePermission(readable = true, writeable = false)
            else -> FilePermission(readable = false, writeable = false)
        }
    }

fun File.copyTo(
    target: File,
    overwrite: Boolean = false,
    bufferSize: Int = DEFAULT_BUFFER_SIZE,
    scope: CoroutineScope? = null,
    listener: ((Float, CoroutineScope?) -> Unit)? = null
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
                input.copyTo(output, bufferSize, this.length(), scope, listener)
            }
        }
    }

    return target
}

suspend fun File.deleteRecursively(listener: (Float, CoroutineScope) -> Unit): Boolean {
    val totalSize = sizeRecursively(FileWalkDirection.BOTTOM_UP)
    var doneSize: Long = 0
    return walkBottomUp().fold(
        true,
        { res, file ->
            doneSize += file.length()
            withContext(Dispatchers.Main) {
                listener.invoke(((doneSize * 100) / totalSize).toFloat(), this)
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

val File.parentDir: File
    get() = this.parentFile ?: if (this.parent != null) File(this.parent!!) else File(this.absolutePath.replaceLast(this.name, String(), true))

fun File.renameTo(newName: String): Boolean {
    val parentDir = this.parentDir
    if (!parentDir.exists() || !this.exists()) {
        return false
    }

    val newFile = File("${parentDir.absolutePath}${File.separator}$newName")

    return this.renameTo(newFile)
}

fun File.intentChooser(context: Context): Intent {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    intent.setDataAndType(this.getProviderUri(context) ?: this.uri, this.getMimeType(context))
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)

    if (androidGreaterOr(Build.VERSION_CODES.KITKAT)) {
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
    }
    if (androidGreaterOr(Build.VERSION_CODES.LOLLIPOP)) {
        intent.addFlags(Intent.FLAG_GRANT_PREFIX_URI_PERMISSION)
    }

    return Intent.createChooser(intent, "Choose App to open file")
}
