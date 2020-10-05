package de.datlag.openfe.util

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.graphics.Color
import android.os.Build
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import de.datlag.openfe.commons.androidGreaterOr
import de.datlag.openfe.enums.AppCategory
import de.datlag.openfe.enums.AppCategory.AUDIO
import de.datlag.openfe.enums.AppCategory.GAME
import de.datlag.openfe.enums.AppCategory.IMAGE
import de.datlag.openfe.enums.AppCategory.MAPS
import de.datlag.openfe.enums.AppCategory.NEWS
import de.datlag.openfe.enums.AppCategory.PRODUCTIVITY
import de.datlag.openfe.enums.AppCategory.SOCIAL
import de.datlag.openfe.enums.AppCategory.UNDEFINED
import de.datlag.openfe.enums.AppCategory.VIDEO
import de.datlag.openfe.enums.AppInstallLocation
import de.datlag.openfe.enums.AppInstallLocation.AUTO
import de.datlag.openfe.enums.AppInstallLocation.INTERNAL_ONLY
import de.datlag.openfe.enums.AppInstallLocation.PREFER_EXTERNAL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object NumberUtils {

    private const val DEFAULT_DATE_FORMAT = "dd. MMM YYYY"

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
    fun getAppCategory(value: Int): AppCategory {
        return when (value) {
            GAME.associatedValue -> GAME
            AUDIO.associatedValue -> AUDIO
            VIDEO.associatedValue -> VIDEO
            IMAGE.associatedValue -> IMAGE
            SOCIAL.associatedValue -> SOCIAL
            NEWS.associatedValue -> NEWS
            MAPS.associatedValue -> MAPS
            PRODUCTIVITY.associatedValue -> PRODUCTIVITY
            else -> UNDEFINED
        }
    }

    @JvmStatic
    fun getAppCategory(value: Long): AppCategory = getAppCategory(value.toInt())

    @JvmStatic
    fun getAppCategory(value: Float): AppCategory = getAppCategory(value.toInt())

    @JvmStatic
    fun getAppCategory(value: Double): AppCategory = getAppCategory(value.toInt())

    @JvmStatic
    fun getAppCategory(applicationInfo: ApplicationInfo): AppCategory = getAppCategory(if (androidGreaterOr(Build.VERSION_CODES.O)) applicationInfo.category else UNDEFINED.associatedValue)

    @JvmStatic
    fun getAppInstallLocation(value: Int): AppInstallLocation {
        return when (value) {
            INTERNAL_ONLY.associatedValue -> INTERNAL_ONLY
            PREFER_EXTERNAL.associatedValue -> PREFER_EXTERNAL
            else -> AUTO
        }
    }

    @JvmStatic
    fun getAppInstallLocation(value: Long): AppInstallLocation = getAppInstallLocation(value.toInt())

    @JvmStatic
    fun getAppInstallLocation(value: Float): AppInstallLocation = getAppInstallLocation(value.toInt())

    @JvmStatic
    fun getAppInstallLocation(value: Double): AppInstallLocation = getAppInstallLocation(value.toInt())

    @JvmStatic
    fun getAppInstallLocation(packageInfo: PackageInfo) = getAppInstallLocation(if (androidGreaterOr(Build.VERSION_CODES.LOLLIPOP)) packageInfo.installLocation else AUTO.associatedValue)

    fun useStatusBarDarkContrast(@ColorInt color: Int): Boolean {
        val whiteContrast = ColorUtils.calculateContrast(Color.WHITE, color)
        val blackContrast = ColorUtils.calculateContrast(Color.BLACK, color)

        return whiteContrast > blackContrast
    }
}
