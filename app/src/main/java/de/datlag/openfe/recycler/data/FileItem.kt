package de.datlag.openfe.recycler.data

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
@Obfuscate
data class FileItem(
    val file: File,
    val name: String? = null
) : Parcelable
