package de.datlag.openfe.commons

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Collections

@Suppress("DEPRECATION")
fun PackageManager.isTelevision(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.hasSystemFeature(PackageManager.FEATURE_TELEVISION) || this.hasSystemFeature(
                PackageManager.FEATURE_LEANBACK
            ) || this.hasSystemFeature(PackageManager.FEATURE_LEANBACK_ONLY)
        } else {
            this.hasSystemFeature(PackageManager.FEATURE_TELEVISION) || this.hasSystemFeature(
                PackageManager.FEATURE_LEANBACK
            )
        }
    } else {
        this.hasSystemFeature(PackageManager.FEATURE_TELEVISION)
    }
}

suspend fun PackageManager.loadAppsAsync(nonSystemOnly: Boolean = true, resultItem: ((Pair<ApplicationInfo, PackageInfo>) -> Unit)? = null) {
    val apps: MutableList<ApplicationInfo> = this.getInstalledApplications(PackageManager.GET_META_DATA)

    Collections.sort(apps, ApplicationInfo.DisplayNameComparator(this))

    val iterator = apps.iterator()
    while (iterator.hasNext()) {
        val nextItem = iterator.next()
        if (nonSystemOnly) {
            if (nextItem.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                iterator.remove()
                continue
            } else if (nextItem.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0) {
                iterator.remove()
                continue
            }
        }

        try {
            if (this.getLaunchIntentForPackage(nextItem.packageName) == null) {
                iterator.remove()
                continue
            }
        } catch (ignored: Exception) { }

        withContext(Dispatchers.Main) {
            resultItem?.invoke(Pair(nextItem, getPackageInfo(nextItem.packageName, PackageManager.GET_META_DATA)))
        }
    }
}
