package de.datlag.openfe.interfaces

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
fun interface FragmentBackPressed {
    fun onBackPressed(): Boolean
}
