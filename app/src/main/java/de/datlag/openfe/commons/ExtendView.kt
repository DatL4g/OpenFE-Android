package de.datlag.openfe.commons

import android.view.View
import android.view.ViewGroup
import de.datlag.openfe.enums.MarginSide
import de.datlag.openfe.enums.MarginSide.BOTTOM
import de.datlag.openfe.enums.MarginSide.LEFT
import de.datlag.openfe.enums.MarginSide.RIGHT
import de.datlag.openfe.enums.MarginSide.TOP

fun View.setMargin(value: Int, side: MarginSide) {
    if (this.layoutParams is ViewGroup.MarginLayoutParams) {
        val marginLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams

        when (side) {
            TOP -> marginLayoutParams.topMargin = value
            LEFT -> marginLayoutParams.leftMargin = value
            RIGHT -> marginLayoutParams.rightMargin = value
            BOTTOM -> marginLayoutParams.bottomMargin = value
        }

        this.layoutParams = marginLayoutParams
        this.requestLayout()
    }
}

fun View.setMargin(value: Int, vararg sides: MarginSide) {
    for (side in sides) {
        this.setMargin(value, side)
    }
}

@JvmOverloads
fun View.setMargin(top: Int = getMargin()[0], left: Int = getMargin()[1], right: Int = getMargin()[2], bottom: Int = getMargin()[3]) {
    this.setMargin(top, TOP)
    this.setMargin(left, LEFT)
    this.setMargin(right, RIGHT)
    this.setMargin(bottom, BOTTOM)
}

fun View.getMargin(): Array<Int> {
    return if (this.layoutParams is ViewGroup.MarginLayoutParams) {
        val marginLayoutParams = this.layoutParams as ViewGroup.MarginLayoutParams
        arrayOf(marginLayoutParams.topMargin, marginLayoutParams.leftMargin, marginLayoutParams.rightMargin, marginLayoutParams.bottomMargin)
    } else {
        arrayOf(0, 0, 0, 0)
    }
}

fun View.show() {
    this.visibility = View.VISIBLE
}

fun View.hide() {
    this.visibility = View.GONE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}
