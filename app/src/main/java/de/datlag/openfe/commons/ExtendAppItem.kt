package de.datlag.openfe.commons

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.annotation.ColorRes
import coil.imageLoader
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import de.datlag.openfe.recycler.data.AppItem

fun AppItem.getFolderIcon(context: Context, @ColorRes backgroundTint: Int, result: (Drawable?) -> Unit) {
    val request = ImageRequest.Builder(context)
        .data(this.icon?.toBitmap()?.fillTransparent()?.applyBorder(15F, context.getColorCompat(backgroundTint)))
        .placeholder(null)
        .transformations(CircleCropTransformation())
        .target(
            onStart = { placeholder ->
                result.invoke(placeholder)
            },
            onSuccess = { resource ->
                result.invoke(resource)
            },
            onError = { error ->
                result.invoke(error)
            }
        ).build()

    context.imageLoader.enqueue(request)
}
