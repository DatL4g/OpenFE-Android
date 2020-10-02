package de.datlag.openfe.extend

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.*
import android.os.Build
import android.util.AttributeSet
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.CallSuper
import androidx.appcompat.widget.LinearLayoutCompat
import de.datlag.openfe.R
import de.datlag.openfe.commons.androidGreaterOr
import de.datlag.openfe.commons.mapOf

class StepProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    private val defaultHeight = resources.getDimensionPixelSize(R.dimen.stepProgressDefaultHeight)

    private var needInitial = true

    var max: Int = DEFAULT_MAX
        get() {
            return if (progressPerBar) {
                progressPerBarList.size
            } else {
                field
            }
        }
        set(value) {
            field = value
            configureStepView()
        }

    var step: Int = DEFAULT_STEP
        set(value) {
            field = value
            configureStepView()
        }

    var stepDoneColor = Color.RED
        set(value) {
            field = value
            configureStepView()
        }

    var stepUndoneColor = Color.WHITE
        set(value) {
            field = value
            configureStepView()
        }

    var stepMargin = resources.getDimensionPixelSize(R.dimen.stepProgressDefaultMargin)
        set(value) {
            field = value
            configureStepView()
        }

    var progressPerBar = false
        set(value) {
            field = value
            configureStepView()
        }

    var progressPerBarList = FloatArray(0)
        set(value) {
            field = value
            if (!progressPerBar) { progressPerBar = true } else { configureStepView() }
        }

    var roundedCorner = resources.getDimensionPixelSize(R.dimen.stepProgressDefaultRoundedCorner)
        set(value) {
            field = value
            configureStepView()
        }

    init {
        orientation = HORIZONTAL
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(
                attrs,
                R.styleable.StepProgressBar, defStyleAttr, 0
            )

            max = typedArray.getInt(R.styleable.StepProgressBar_max, max)
            step = typedArray.getInt(R.styleable.StepProgressBar_step, step)
            stepDoneColor = typedArray.getColor(R.styleable.StepProgressBar_stepDoneColor, stepDoneColor)
            stepUndoneColor = typedArray.getColor(R.styleable.StepProgressBar_stepUndoneColor, stepUndoneColor)
            stepMargin = typedArray.getDimensionPixelSize(R.styleable.StepProgressBar_stepMargin, stepMargin)
            roundedCorner = typedArray.getDimensionPixelSize(R.styleable.StepProgressBar_cornerRadius, roundedCorner)

            typedArray.recycle()
        }
    }

    fun updateProgressPerBarList(newArray: FloatArray) {
        if (progressPerBarList.size != newArray.size) {
            progressPerBarList = newArray.copyOf()
            return
        }

        val map: Map<Float, Float> = mapOf(progressPerBarList.toTypedArray() to newArray.toTypedArray())
        val changeList: Map<Float, Float> = map.filter {
            it.key != it.value
        }
        val reconfigureList: Map<Float, Float> = changeList.filter {
            it.key == 100F || it.value == 100F
        }

        if (reconfigureList.isNotEmpty()) {
            progressPerBarList = newArray.copyOf()
            return
        }

        val newList = changeList.toList()
        for (position in newList.indices) {
            val progressBar: ProgressBar? = getChildAt(position + if (newList.size == childCount) 0 else 1) as? ProgressBar?
            progressBar?.progress = newList[position].second.toInt()
        }
    }

    private fun configureStepView(width: Int = getWidth(), height: Int = getHeight()) {
        if (needInitial) {
            return
        }

        removeAllViewsInLayout()

        val totalViewWidth = width - stepMargin * (max - 1)
        val undoneViewWidth = totalViewWidth / max
        val undoneStepCount = if (progressPerBar) {
            var undone = 0
            for (item in progressPerBarList) {
                if (item == 100F) {
                    undone += 1
                }
            }
            max - undone
        } else {
            max - step
        }
        val doneViewWidth = width - undoneStepCount * (undoneViewWidth + stepMargin)

        if (doneViewWidth > 0) {
            addView(createDoneView(doneViewWidth, height))
        }
        repeat(undoneStepCount) {
            addView(createUndoneView(undoneViewWidth, height, it, doneViewWidth > 0))
        }
    }

    private fun getDefaultHeight(size: Int, measureSpec: Int): Int {
        val specMode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when(specMode) {
            MeasureSpec.EXACTLY -> specSize
            MeasureSpec.UNSPECIFIED, MeasureSpec.AT_MOST -> size
            else -> size
        }
    }

    private fun createDoneView(doneViewWidth: Int, height: Int): View {
        return if (progressPerBar) ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LayoutParams(doneViewWidth, height)
            isIndeterminate = false
            progressDrawable = progressLayerDrawable()
            max = 1
            progress = 1
        } else View(context).apply {
            layoutParams = LayoutParams(doneViewWidth, height)
            background = getDrawables().first
        }
    }

    private fun createUndoneView(stepItemWidth: Int, height: Int, position: Int = 0, doneViewExists: Boolean = false): View {
        return if (progressPerBar) ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal).apply {
            layoutParams = LayoutParams(stepItemWidth, height).apply {
                if (doneViewExists) {
                    leftMargin = stepMargin
                }
            }
            isIndeterminate = false
            progressDrawable = progressLayerDrawable()
            max = 100
            progress = progressPerBarList.filter { it != 100F }[position].toInt()
        } else View(context).apply {
            layoutParams = LayoutParams(stepItemWidth, height).apply {
                if (doneViewExists) {
                    leftMargin = stepMargin
                }
            }
            background = getDrawables().second
        }
    }

    private fun progressLayerDrawable(): Drawable {
        val drawables = getDrawables()
        val backgroundShape = ShapeDrawable(drawables.second.shape)
        backgroundShape.paint.style = drawables.first.paint.style
        backgroundShape.paint.color = drawables.second.paint.color

        val foregroundShape = ShapeDrawable(drawables.first.shape)
        foregroundShape.paint.style = drawables.first.paint.style
        foregroundShape.paint.color = drawables.first.paint.color

        val clip = ClipDrawable(foregroundShape, Gravity.START, ClipDrawable.HORIZONTAL)
        return LayerDrawable(arrayOf(backgroundShape, clip))
    }

    private fun getDrawables(): Pair<PaintDrawable, PaintDrawable> {
        return Pair(
            PaintDrawable(stepDoneColor).apply {
                setCornerRadius(roundedCorner.toFloat())
            },
            PaintDrawable(stepUndoneColor).apply {
                setCornerRadius(roundedCorner.toFloat())
            })
    }

    @CallSuper
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(suggestedMinimumWidth, widthMeasureSpec)
        val height = getDefaultHeight(defaultHeight, heightMeasureSpec)
        super.onMeasure(width, height)
        if (needInitial) {
            needInitial = false
            configureStepView(width, height)
        }
    }

    companion object {
        private const val DEFAULT_MAX = 0
        private const val DEFAULT_STEP = 0
    }
}