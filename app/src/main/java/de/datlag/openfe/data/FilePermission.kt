package de.datlag.openfe.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class FilePermission(
    val readable: Boolean,
    val writeable: Boolean
) : Parcelable
