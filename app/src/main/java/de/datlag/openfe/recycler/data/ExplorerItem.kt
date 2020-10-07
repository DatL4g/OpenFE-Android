package de.datlag.openfe.recycler.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExplorerItem(
    val fileItem: FileItem,
    var appItem: AppItem? = null,
    val selectable: Boolean = true,
    var selected: Boolean = false
) : Parcelable
