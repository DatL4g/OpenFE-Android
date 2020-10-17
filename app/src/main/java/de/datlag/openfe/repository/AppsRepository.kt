package de.datlag.openfe.repository

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import de.datlag.openfe.commons.loadAppsAsync
import io.michaelrocks.paranoid.Obfuscate
import javax.inject.Inject

@Obfuscate
class AppsRepository @Inject constructor(
    val packageManager: PackageManager
) {

    suspend fun loadApps(nonSystemOnly: Boolean = true, resultItem: ((Pair<ApplicationInfo, PackageInfo>) -> Unit)? = null) = packageManager.loadAppsAsync(nonSystemOnly, resultItem)
}
