package de.datlag.openfe.interfaces

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
fun interface FragmentNoAdPermission {

    fun onNoAdPermissionChanged(permitted: Boolean)
}
