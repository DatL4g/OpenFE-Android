package de.datlag.openfe.safeargs

import android.os.Parcelable
import de.datlag.openfe.filter.MimeTypeFilter
import de.datlag.openfe.recycler.data.LocationItem
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.parcel.Parcelize

@Parcelize
@Obfuscate
data class StorageArgs(
    val list: List<LocationItem>,
    val selected: Int,
    val mimeTypeFilter: MimeTypeFilter? = null
) : Parcelable
