package de.datlag.openfe.commons

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import de.datlag.openfe.recycler.data.AppItem

fun AppItem.getFolderIcon(context: Context, @ColorRes backgroundTint: Int, result: (Drawable?) -> Unit) {
    Glide.with(context)
        .asBitmap()
        .placeholder(null)
        .load(this.icon?.toBitmap()?.fillTransparent()?.applyBorder(15F, context.getColorCompat(backgroundTint)))
        .apply(RequestOptions.circleCropTransform())
        .into(object : CustomTarget<Bitmap>() {
            override fun onLoadStarted(placeholder: Drawable?) {
                super.onLoadStarted(placeholder)
                result.invoke(placeholder)
            }

            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?
            ) {
                result.invoke(resource.toDrawable(context.resources))
            }

            override fun onLoadCleared(placeholder: Drawable?) {
                result.invoke(placeholder)
            }

            override fun onLoadFailed(errorDrawable: Drawable?) {
                super.onLoadFailed(errorDrawable)
                result.invoke(errorDrawable)
            }
        })
}
