package de.datlag.openfe.recycler.data

data class ExplorerItem(
    val fileItem: FileItem,
    var appItem: AppItem? = null,
    val selectable: Boolean = true,
    var selected: Boolean = false
)
