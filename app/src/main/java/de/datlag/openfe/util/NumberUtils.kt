package de.datlag.openfe.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.Color
import android.os.Build
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.IntDef
import androidx.core.graphics.ColorUtils
import de.datlag.openfe.commons.androidGreaterOr
import java.text.SimpleDateFormat
import java.util.*

object NumberUtils {

    private const val DEFAULT_DATE_FORMAT = "dd. MMM YYYY"

    const val CATEGORY_UNDEFINED = -1
    const val CATEGORY_GAME = 0
    const val CATEGORY_AUDIO = 1
    const val CATEGORY_VIDEO = 2
    const val CATEGORY_IMAGE = 3
    const val CATEGORY_SOCIAL = 4
    const val CATEGORY_NEWS = 5
    const val CATEGORY_MAPS = 6
    const val CATEGORY_PRODUCTIVITY = 7

    const val INSTALL_LOCATION_AUTO = 0
    const val INSTALL_LOCATION_INTERNAL_ONLY = 1
    const val INSTALL_LOCATION_PREFER_EXTERNAL = 2

    @IntDef(CATEGORY_UNDEFINED, CATEGORY_GAME, CATEGORY_AUDIO, CATEGORY_VIDEO, CATEGORY_IMAGE, CATEGORY_SOCIAL, CATEGORY_NEWS, CATEGORY_MAPS, CATEGORY_PRODUCTIVITY)
    @Retention(AnnotationRetention.SOURCE)
    annotation class AppCategory

    @IntDef(INSTALL_LOCATION_AUTO, INSTALL_LOCATION_INTERNAL_ONLY, INSTALL_LOCATION_PREFER_EXTERNAL)
    @Retention(AnnotationRetention.SOURCE)
    annotation class AppInstallLocation


    @JvmStatic
    @JvmOverloads
    fun convertToDate(milli: Long, format: String = DEFAULT_DATE_FORMAT): String {
        return SimpleDateFormat(format, Locale.getDefault()).format(Date(milli))
    }

    @JvmStatic
    @JvmOverloads
    fun convertToDate(milli: Int, format: String = DEFAULT_DATE_FORMAT) = convertToDate(milli.toLong(), format)

    @JvmStatic
    @JvmOverloads
    fun convertToDate(milli: Float, format: String = DEFAULT_DATE_FORMAT) = convertToDate(milli.toLong(), format)

    @JvmStatic
    @JvmOverloads
    fun convertToDate(milli: Double, format: String = DEFAULT_DATE_FORMAT) = convertToDate(milli.toLong(), format)

    @JvmStatic
    @AppCategory
    fun getAppCategory(value: Int): Int {
        return when(value) {
            CATEGORY_GAME -> CATEGORY_GAME
            CATEGORY_AUDIO -> CATEGORY_AUDIO
            CATEGORY_VIDEO -> CATEGORY_VIDEO
            CATEGORY_IMAGE -> CATEGORY_IMAGE
            CATEGORY_SOCIAL -> CATEGORY_SOCIAL
            CATEGORY_NEWS -> CATEGORY_NEWS
            CATEGORY_MAPS -> CATEGORY_MAPS
            CATEGORY_PRODUCTIVITY -> CATEGORY_PRODUCTIVITY
            else -> CATEGORY_UNDEFINED
        }
    }

    @JvmStatic
    @AppCategory
    fun getAppCategory(value: Long) = getAppCategory(value.toInt())

    @JvmStatic
    @AppCategory
    fun getAppCategory(value: Float) = getAppCategory(value.toInt())

    @JvmStatic
    @AppCategory
    fun getAppCategory(value: Double) = getAppCategory(value.toInt())

    @JvmStatic
    @AppCategory
    fun getAppCategory(applicationInfo: ApplicationInfo) = getAppCategory(if (androidGreaterOr(Build.VERSION_CODES.O)) applicationInfo.category else CATEGORY_UNDEFINED)

    @JvmStatic
    @AppInstallLocation
    fun getAppInstallLocation(value: Int): Int {
        return when(value) {
            INSTALL_LOCATION_INTERNAL_ONLY -> INSTALL_LOCATION_INTERNAL_ONLY
            INSTALL_LOCATION_PREFER_EXTERNAL -> INSTALL_LOCATION_PREFER_EXTERNAL
            else -> INSTALL_LOCATION_AUTO
        }
    }

    @JvmStatic
    @AppInstallLocation
    fun getAppInstallLocation(value: Long) = getAppInstallLocation(value.toInt())

    @JvmStatic
    @AppInstallLocation
    fun getAppInstallLocation(value: Float) = getAppInstallLocation(value.toInt())

    @JvmStatic
    @AppInstallLocation
    fun getAppInstallLocation(value: Double) = getAppInstallLocation(value.toInt())

    @JvmStatic
    @AppInstallLocation
    fun getAppInstallLocation(packageInfo: PackageInfo) =getAppInstallLocation(if (androidGreaterOr(Build.VERSION_CODES.LOLLIPOP)) packageInfo.installLocation else INSTALL_LOCATION_AUTO)

    fun useStatusBarDarkContrast(@ColorInt color: Int): Boolean {
        val whiteContrast = ColorUtils.calculateContrast(Color.WHITE, color)
        val blackContrast = ColorUtils.calculateContrast(Color.BLACK, color)

        return whiteContrast > blackContrast
    }
}