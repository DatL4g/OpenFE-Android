package de.datlag.openfe.recycler.data

import android.graphics.drawable.Drawable

data class ExplorerItem(
    val fileItem: FileItem,
    val appIcon: Drawable? = null,
    val selectable: Boolean = true,
    var selected: Boolean = false
)