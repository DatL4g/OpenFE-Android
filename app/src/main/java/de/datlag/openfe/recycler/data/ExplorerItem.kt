package de.datlag.openfe.recycler.data

import android.os.Parcelable
import de.datlag.openfe.commons.matchWithApps
import de.datlag.openfe.viewmodel.AppList
import io.michaelrocks.paranoid.Obfuscate
import kotlinx.android.parcel.Parcelize
import java.io.File

@Parcelize
@Obfuscate
data class ExplorerItem(
    val fileItem: FileItem,
    var appItem: AppItem? = null,
    val selectable: Boolean = true,
    var selected: Boolean = false
) : Parcelable {

    companion object {
        fun from(fileItem: FileItem, appList: AppList): ExplorerItem {
            return ExplorerItem(fileItem).apply { matchWithApps(appList) }
        }

        fun from(file: File, appList: AppList): ExplorerItem = from(FileItem(file), appList)
    }
}
