@file:Obfuscate
package de.datlag.openfe.commons

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import io.michaelrocks.paranoid.Obfuscate
import kotlin.math.min

fun Bitmap.applyBorder(borderSize: Float = 2F, borderColor: Int = Color.WHITE): Bitmap {
    val borderOffset = (borderSize * 2).toInt()
    val halfWidth = width / 2
    val halfHeight = height / 2
    val circleRadius = min(halfWidth, halfHeight).toFloat()
    val newBitmap = Bitmap.createBitmap(width + borderOffset, height + borderOffset, Bitmap.Config.ARGB_8888)

    val centerX = halfWidth + borderSize
    val centerY = halfHeight + borderSize

    val paint = Paint()
    val canvas = Canvas(newBitmap).apply { drawARGB(0, 0, 0, 0) }

    paint.isAntiAlias = true
    paint.style = Paint.Style.FILL
    canvas.drawCircle(centerX, centerY, circleRadius, paint)

    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    canvas.drawBitmap(this, borderSize, borderSize, paint)

    paint.xfermode = null
    paint.style = Paint.Style.STROKE
    paint.color = borderColor
    paint.strokeWidth = borderSize
    canvas.drawCircle(centerX, centerY, circleRadius, paint)
    return newBitmap
}

fun Bitmap.fillTransparent(color: Int = Color.WHITE): Bitmap {
    val bitmap = Bitmap.createBitmap(this)
    val canvas = Canvas(bitmap.apply { eraseColor(Color.TRANSPARENT) })
    canvas.drawColor(color)
    canvas.drawBitmap(this, 0F, 0F, null)
    return bitmap
}
