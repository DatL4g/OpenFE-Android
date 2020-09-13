package de.datlag.openfe.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class Usage(
    val file: File,
    val max: Long,
    val current: Long,
    val percentage: Float
) : Parcelable