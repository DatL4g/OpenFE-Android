package de.datlag.openfe.recycler.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
data class FileItem(
    val file: File,
    val name: String? = null
) : Parcelable
