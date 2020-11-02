package de.datlag.openfe.interfaces

import de.datlag.openfe.recycler.data.AppItem

fun interface FragmentSystemAppsLoaded {
    fun onSystemAppsLoaded(apps: List<AppItem>)
}
