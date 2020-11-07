package de.datlag.openfe.filter

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class MimeTypeFilter(
    val acceptApplication: Boolean = false,
    val acceptArchive: Boolean = false,
    val acceptAudio: Boolean = false,
    val acceptDocument: Boolean = false,
    val acceptFont: Boolean = false,
    val acceptImage: Boolean = false,
    val acceptText: Boolean = false,
    val acceptVideo: Boolean = false
) : Parcelable
