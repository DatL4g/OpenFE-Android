package de.datlag.openfe.viewmodel

import androidx.lifecycle.ViewModel
import de.datlag.openfe.fragments.actions.AppsFragmentArgs
import de.datlag.openfe.recycler.data.AppItem
import io.michaelrocks.paranoid.Obfuscate
import java.io.File

@Obfuscate
class AppsActionViewModel(appsActionArgs: AppsFragmentArgs) : ViewModel() {

    val storageFile = File(appsActionArgs.filePath)
    var selectedApp: AppItem? = null
}
