package de.datlag.openfe.enums

import android.content.Context

enum class AppInstallLocation(val associatedValue: Int) {

    AUTO(0),
    INTERNAL_ONLY(1),
    PREFER_EXTERNAL(2);

    fun toString(context: Context): String {
        return when (this) {
            INTERNAL_ONLY -> "Audio"
            PREFER_EXTERNAL -> "Video"
            else -> "Automatic"
        }
    }
}
