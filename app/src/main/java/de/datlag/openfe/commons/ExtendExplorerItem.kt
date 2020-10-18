@file:Obfuscate
package de.datlag.openfe.commons

import de.datlag.openfe.recycler.data.ExplorerItem
import de.datlag.openfe.viewmodel.AppList
import io.michaelrocks.paranoid.Obfuscate

fun ExplorerItem.matchWithApps(appList: AppList) {
    val fileItem = this.fileItem
    try {
        for (appItem in appList) {
            if (fileItem.name?.equals(appItem.name, true) == true ||
                fileItem.name?.equals(appItem.packageName, true) == true ||
                fileItem.file.name.equals(appItem.name, true) ||
                fileItem.file.name.equals(appItem.packageName, true) ||
                fileItem.file.path.equals(appItem.sourceDir, true) ||
                fileItem.file.path.equals(appItem.publicSourceDir, true) ||
                fileItem.file.path.equals(appItem.dataDir, true) ||
                fileItem.file.absolutePath.equals(appItem.sourceDir, true) ||
                fileItem.file.absolutePath.equals(appItem.publicSourceDir, true) ||
                fileItem.file.absolutePath.equals(appItem.dataDir, true)
            ) {
                this.appItem = appItem
            }
        }
    } catch (exception: Exception) { }
}
