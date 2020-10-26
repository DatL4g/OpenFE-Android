package de.datlag.openfe.interfaces

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
fun interface FragmentOAuthCallback {
    fun onAuthCode(code: String?)
}
