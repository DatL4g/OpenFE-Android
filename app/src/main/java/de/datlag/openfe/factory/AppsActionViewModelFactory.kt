package de.datlag.openfe.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import de.datlag.openfe.fragments.actions.AppsFragmentArgs
import de.datlag.openfe.viewmodel.AppsActionViewModel

class AppsActionViewModelFactory(
    private val appsActionArgs: AppsFragmentArgs,
) : ViewModelProvider.NewInstanceFactory() {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(
        modelClass: Class<T>
    ) = AppsActionViewModel(appsActionArgs) as T
}
