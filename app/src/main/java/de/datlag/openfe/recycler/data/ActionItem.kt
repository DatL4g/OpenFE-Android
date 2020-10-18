package de.datlag.openfe.recycler.data

import android.graphics.drawable.Drawable
import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
@Obfuscate
data class ActionItem(
    val icon: @RawValue Drawable?,
    val name: String,
    val action: @RawValue () -> Unit
) : Parcelable
