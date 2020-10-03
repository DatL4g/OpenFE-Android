package de.datlag.openfe.extend

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

open class ReverseFrameLayout @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : FrameLayout(context, attrs) {

    init {
        isChildrenDrawingOrderEnabled = true
    }

    override fun getChildDrawingOrder(childCount: Int, drawingPosition: Int): Int {
        return childCount - 1 - drawingPosition
    }
}
