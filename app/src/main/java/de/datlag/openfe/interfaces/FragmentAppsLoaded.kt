package de.datlag.openfe.interfaces

import de.datlag.openfe.recycler.data.AppItem

fun interface FragmentAppsLoaded {
    fun onAppsLoaded(apps: List<AppItem>)
}
