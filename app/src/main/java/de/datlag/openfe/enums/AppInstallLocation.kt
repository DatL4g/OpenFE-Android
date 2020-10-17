package de.datlag.openfe.enums

import android.content.Context
import de.datlag.openfe.R
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
enum class AppInstallLocation(val associatedValue: Int) {

    AUTO(0),
    INTERNAL_ONLY(1),
    PREFER_EXTERNAL(2);

    fun toString(context: Context): String {
        return when (this) {
            INTERNAL_ONLY -> context.getString(R.string.app_install_location_internal)
            PREFER_EXTERNAL -> context.getString(R.string.app_install_location_external)
            else -> context.getString(R.string.app_install_location_auto)
        }
    }
}
