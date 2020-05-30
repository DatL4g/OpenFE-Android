package de.datlag.openfe.commons

import android.content.Context
import android.os.Build
import android.view.WindowManager
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

val Fragment.saveContext: Context
    get() {
        return this.context ?: this.activity ?: this.requireContext()
    }

fun Fragment.statusBarColor(@ColorInt color: Int) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        activity?.window?.statusBarColor = color
    }
}

fun Fragment.getColor(@ColorRes color: Int) = ContextCompat.getColor(saveContext, color)

fun Fragment.getDrawable(@DrawableRes drawable: Int) = ContextCompat.getDrawable(saveContext, drawable)

fun Fragment.getColorStateList(@ColorRes colorStateList: Int) = ContextCompat.getColorStateList(saveContext, colorStateList)

fun Fragment.showBottomSheetFragment(bottomSheet: BottomSheetDialogFragment) = bottomSheet.show(childFragmentManager, bottomSheet.tag)