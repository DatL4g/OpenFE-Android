package de.datlag.openfe.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.datlag.openfe.fragments.ExplorerFragmentArgs
import de.datlag.openfe.viewmodel.BackupViewModel
import de.datlag.openfe.viewmodel.ExplorerViewModel
import io.michaelrocks.paranoid.Obfuscate
import kotlin.contracts.ExperimentalContracts

@ExperimentalContracts
@Obfuscate
class ExplorerViewModelFactory(
    private val explorerArgs: ExplorerFragmentArgs,
    private val backupViewModel: BackupViewModel
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        modelClass: Class<T>
    ) = ExplorerViewModel(explorerArgs, backupViewModel) as T
}
