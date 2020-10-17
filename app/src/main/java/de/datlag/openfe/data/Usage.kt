package de.datlag.openfe.data

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
@Obfuscate
data class Usage(
    val file: File,
    val max: Long,
    val current: Long,
    val percentage: Float
) : Parcelable
