package de.datlag.openfe.provider

import io.michaelrocks.paranoid.Obfuscate
import javax.crypto.Cipher

@Obfuscate
interface CipherProvider {
    val encryptCipher: Cipher
    fun decryptCipher(initialVector: ByteArray): Cipher
}
