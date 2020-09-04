package de.datlag.openfe.commons

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import de.datlag.openfe.R

fun Drawable.tint(context: Context): Drawable {
    var wrappedDrawable = this.mutate()
    wrappedDrawable = DrawableCompat.wrap(wrappedDrawable)
    DrawableCompat.setTint(wrappedDrawable, ContextCompat.getColor(context, R.color.explorerIconTint))
    DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN)
    return wrappedDrawable
}