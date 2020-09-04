package de.datlag.openfe.interfaces

import android.view.Menu
import android.view.MenuInflater

fun interface FragmentOptionsMenu {
    fun onCreateMenu(menu: Menu?, inflater: MenuInflater): Boolean
}