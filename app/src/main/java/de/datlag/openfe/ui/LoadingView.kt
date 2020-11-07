package de.datlag.openfe.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.vectordrawable.graphics.drawable.Animatable2Compat
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
import de.datlag.openfe.R

class LoadingView @JvmOverloads constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int = 0) : FrameLayout(context, attrs, defStyleAttr) {

    private var animatable2Compat: Animatable2Compat? = null
        set(value) {
            field = value
            if (started) {
                startAnimation()
            }
        }

    private var started: Boolean = false

    init {
        inflate(context, R.layout.layout_loading_view, this)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        createAnimatable()
    }

    private fun createAnimatable() {
        val animatedVectorDrawableCompat = AnimatedVectorDrawableCompat.create(
            this.context,
            R.drawable.ic_file_animation
        )
        val loadingImageView = findViewById<AppCompatImageView>(R.id.loadingImageView)
        loadingImageView.setImageDrawable(animatedVectorDrawableCompat)
        animatable2Compat = loadingImageView.drawable as Animatable2Compat
    }

    fun startAnimation() {
        started = true
        animatable2Compat?.let {
            if (!it.isRunning) {
                it.registerAnimationCallback(object : Animatable2Compat.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        super.onAnimationEnd(drawable)
                        it.start()
                    }
                })
                it.start()
            }
        }
    }

    fun stopAnimation() {
        started = false
        animatable2Compat?.let {
            if (it.isRunning) {
                it.stop()
            }
        }
    }
}
