package de.datlag.openfe.recycler.data

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import de.datlag.openfe.commons.androidGreaterOr
import de.datlag.openfe.enums.AppCategory
import de.datlag.openfe.enums.AppInstallLocation
import de.datlag.openfe.util.NumberUtils.getAppCategory
import de.datlag.openfe.util.NumberUtils.getAppInstallLocation

data class AppItem(
    val icon: Drawable?,
    val name: String,
    val description: String,
    val packageName: String,
    val category: AppCategory,
    val sourceDir: String,
    val publicSourceDir: String,
    val dataDir: String,
    val splitSourceDirs: Array<String>,
    val splitPublicSourceDirs: Array<String>,
    val installLocation: AppInstallLocation,
    val firstInstall: Long,
    val lastUpdate: Long,
    val versionCode: Long,
    val versionName: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as AppItem

        if (icon != other.icon) return false
        if (name != other.name) return false
        if (description != other.description) return false
        if (packageName != other.packageName) return false
        if (category != other.category) return false
        if (sourceDir != other.sourceDir) return false
        if (publicSourceDir != other.publicSourceDir) return false
        if (dataDir != other.dataDir) return false
        if (!splitSourceDirs.contentEquals(other.splitSourceDirs)) return false
        if (!splitPublicSourceDirs.contentEquals(other.splitPublicSourceDirs)) return false
        if (installLocation != other.installLocation) return false
        if (firstInstall != other.firstInstall) return false
        if (lastUpdate != other.lastUpdate) return false
        if (versionCode != other.versionCode) return false
        if (versionName != other.versionName) return false

        return true
    }

    override fun hashCode(): Int {
        var result = icon?.hashCode() ?: 0
        result = 31 * result + name.hashCode()
        result = 31 * result + description.hashCode()
        result = 31 * result + packageName.hashCode()
        result = 31 * result + category.hashCode()
        result = 31 * result + sourceDir.hashCode()
        result = 31 * result + publicSourceDir.hashCode()
        result = 31 * result + dataDir.hashCode()
        result = 31 * result + splitSourceDirs.contentHashCode()
        result = 31 * result + splitPublicSourceDirs.contentHashCode()
        result = 31 * result + installLocation.hashCode()
        result = 31 * result + firstInstall.hashCode()
        result = 31 * result + lastUpdate.hashCode()
        result = 31 * result + versionCode.hashCode()
        result = 31 * result + versionName.hashCode()
        return result
    }

    companion object {

        @JvmStatic
        @JvmOverloads
        @Suppress("DEPRECATION")
        fun from(packageManager: PackageManager, packageInfo: PackageInfo, applicationInfo: ApplicationInfo = packageInfo.applicationInfo): AppItem {
            return AppItem(
                applicationInfo.loadIcon(packageManager),
                applicationInfo.loadLabel(packageManager).toString(),
                applicationInfo.loadDescription(packageManager)?.toString().toString(),
                applicationInfo.packageName,
                getAppCategory(applicationInfo),
                applicationInfo.sourceDir,
                applicationInfo.publicSourceDir,
                applicationInfo.dataDir,
                if (androidGreaterOr(Build.VERSION_CODES.LOLLIPOP)) applicationInfo.splitSourceDirs ?: arrayOf() else arrayOf(),
                if (androidGreaterOr(Build.VERSION_CODES.LOLLIPOP)) applicationInfo.splitPublicSourceDirs ?: arrayOf() else arrayOf(),
                getAppInstallLocation(packageInfo),
                packageInfo.firstInstallTime,
                packageInfo.lastUpdateTime,
                if (androidGreaterOr(Build.VERSION_CODES.P)) packageInfo.longVersionCode else packageInfo.versionCode.toLong(),
                packageInfo.versionName
            )
        }

        @JvmStatic
        @JvmOverloads
        fun from(packageManager: PackageManager, packageName: String, method: Int = PackageManager.GET_META_DATA): AppItem = from(packageManager, packageManager.getPackageInfo(packageName, method))
    }
}
