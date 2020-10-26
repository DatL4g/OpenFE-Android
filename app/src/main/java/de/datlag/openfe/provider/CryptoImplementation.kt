package de.datlag.openfe.provider

import io.michaelrocks.paranoid.Obfuscate
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

@Obfuscate
class CryptoImplementation @Inject constructor(
    private val cipherProvider: CipherProvider
) : Crypto {

    override fun encrypt(rawBytes: ByteArray, outputStream: OutputStream) {
        val cipher = cipherProvider.encryptCipher
        val encryptedBytes = cipher.doFinal(rawBytes)
        with(outputStream) {
            write(cipher.iv.size)
            write(cipher.iv)
            write(encryptedBytes.size)
            write(encryptedBytes)
        }
    }

    override fun decrypt(inputStream: InputStream): ByteArray {
        val initialVectorSize = inputStream.read()
        val initialVector = ByteArray(initialVectorSize)
        inputStream.read(initialVector)

        val encryptedDataSize = inputStream.read()
        val encryptedData = ByteArray(encryptedDataSize)
        inputStream.read(encryptedData)

        val cipher = cipherProvider.decryptCipher(initialVector)
        return cipher.doFinal(encryptedData)
    }
}
