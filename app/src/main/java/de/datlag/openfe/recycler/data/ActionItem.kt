package de.datlag.openfe.recycler.data

import android.graphics.drawable.Drawable
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
data class ActionItem(
    val icon: @RawValue Drawable?,
    val name: String,
    val actionId: Int
) : Parcelable
