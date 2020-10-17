package de.datlag.openfe.data

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.parcel.Parcelize

@Parcelize
@Obfuscate
data class FilePermission(
    val readable: Boolean,
    val writeable: Boolean
) : Parcelable
