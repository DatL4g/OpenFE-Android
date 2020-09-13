package de.datlag.openfe.data

import android.os.Parcelable
import de.datlag.openfe.recycler.data.LocationItem
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ExplorerFragmentStorageArgs(
    val list: List<LocationItem>,
    val item: Int
) : Parcelable