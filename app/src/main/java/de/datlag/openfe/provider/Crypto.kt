package de.datlag.openfe.provider

import io.michaelrocks.paranoid.Obfuscate
import java.io.InputStream
import java.io.OutputStream

@Obfuscate
interface Crypto {
    fun encrypt(rawBytes: ByteArray, outputStream: OutputStream)
    fun decrypt(inputStream: InputStream): ByteArray
}
