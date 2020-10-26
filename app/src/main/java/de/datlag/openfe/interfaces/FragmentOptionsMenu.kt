package de.datlag.openfe.interfaces

import android.view.Menu
import android.view.MenuInflater
import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
fun interface FragmentOptionsMenu {
    fun onCreateMenu(menu: Menu?, inflater: MenuInflater): Boolean
}
