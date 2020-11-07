@file:Obfuscate
package de.datlag.openfe.commons

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.annotation.ColorRes
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import de.datlag.mimemagic.commons.getMimeData
import de.datlag.openfe.R
import de.datlag.openfe.models.FilePermission
import de.datlag.openfe.models.Usage
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
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
    intent.setDataAndType(this.getProviderUri(context) ?: this.uri, this.getMimeData(context).mimeType)
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

@ExperimentalContracts
fun File.getIcon(context: Context, isApkChecked: Pair<Boolean, Boolean>, @ColorRes tint: Int, result: (Drawable?) -> Unit) {
    val isApk = if (isApkChecked.first) {
        isApkChecked.second
    } else {
        this.isAPK()
    }

    val fallback = if (isApk) {
        context.getDrawableCompat(R.drawable.ic_adb_24dp, context.getColorCompat(tint))
    } else {
        context.getDrawableCompat(R.drawable.ic_baseline_insert_drive_file_24, context.getColorCompat(tint))
    }

    val fileTarget = object : CustomTarget<Drawable>() {
        override fun onLoadStarted(placeholder: Drawable?) {
            super.onLoadStarted(placeholder)
            result.invoke(placeholder)
        }

        override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
            result.invoke(resource)
        }

        override fun onLoadCleared(placeholder: Drawable?) {
            result.invoke(placeholder)
        }

        override fun onLoadFailed(errorDrawable: Drawable?) {
            super.onLoadFailed(errorDrawable)
            result.invoke(errorDrawable)
        }
    }

    when {
        this.isDirectory -> {
            Glide.with(context)
                .load(context.getDrawableCompat(R.drawable.ic_baseline_folder_24, context.getColorCompat(tint)))
                .into(fileTarget)
        }
        isApk -> {
            val icon = this.getAPKImage(context, true)
            Glide.with(context)
                .load(icon)
                .fallback(fallback)
                .placeholder(fallback)
                .error(fallback)
                .apply(RequestOptions.circleCropTransform())
                .into(fileTarget)
        }
        else -> {
            Glide.with(context)
                .load(this.uri)
                .fallback(fallback)
                .placeholder(fallback)
                .error(fallback)
                .into(fileTarget)
        }
    }
}

fun File.moveTo(destination: File) = this.renameTo(destination)
