@file:Obfuscate
package de.datlag.openfe.commons

import android.content.Context
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ContextThemeWrapper
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import de.datlag.openfe.R
import de.datlag.openfe.util.NumberUtils.useStatusBarDarkContrast
import io.michaelrocks.paranoid.Obfuscate

val Fragment.safeContext: Context
    get() = this.context ?: this.activity ?: this.requireContext()

val Fragment.supportActionBar: ActionBar?
    get() = (this.activity as? AppCompatActivity?)?.supportActionBar

fun Fragment.getThemedLayoutInflater(inflater: LayoutInflater = this.layoutInflater, @StyleRes themeResId: Int = R.style.FragmentTheme): LayoutInflater {
    val contextThemeWrapper = ContextThemeWrapper(safeContext, themeResId)
    safeContext.theme.applyStyle(themeResId, true)
    return inflater.cloneInContext(contextThemeWrapper)
}

@Suppress("DEPRECATION")
fun Fragment.statusBarColor(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activity?.window?.statusBarColor = color
    }

    if (androidGreaterOr(Build.VERSION_CODES.R)) {
        if (!useStatusBarDarkContrast(color)) {
            activity?.window?.insetsController?.setSystemBarsAppearance(WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            activity?.window?.insetsController?.setSystemBarsAppearance(0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        }
    } else if (androidGreaterOr(Build.VERSION_CODES.M)) {
        if (!useStatusBarDarkContrast(color)) {
            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
    }
}

fun Fragment.getColor(@ColorRes color: Int) = safeContext.getColorCompat(color)

fun Fragment.getDrawable(@DrawableRes drawable: Int) = safeContext.getDrawableCompat(drawable)

fun Fragment.getDrawable(@DrawableRes drawable: Int, @ColorInt tint: Int) = safeContext.getDrawableCompat(drawable, tint)

fun Fragment.showBottomSheetFragment(bottomSheet: BottomSheetDialogFragment) = bottomSheet.show(
    childFragmentManager,
    bottomSheet.tag
)
