package de.datlag.openfe.commons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.DrawableCompat

fun Drawable.tint(color: Int): Drawable {
    var wrappedDrawable = this.mutate()
    wrappedDrawable = DrawableCompat.wrap(wrappedDrawable)
    DrawableCompat.setTint(wrappedDrawable, color)
    DrawableCompat.setTintMode(wrappedDrawable, PorterDuff.Mode.SRC_IN)
    return wrappedDrawable
}

fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    this.setBounds(0, 0, canvas.width, canvas.height)
    this.draw(canvas)
    return bitmap
}
