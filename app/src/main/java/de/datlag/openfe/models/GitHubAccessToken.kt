package de.datlag.openfe.models

import android.os.Parcelable
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@Obfuscate
@Parcelize
data class GitHubAccessToken(
    @SerialName("access_token") val token: String,
    @SerialName("token_type") val type: String,
    val scope: String
) : Parcelable
