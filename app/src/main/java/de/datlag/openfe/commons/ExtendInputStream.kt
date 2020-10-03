package de.datlag.openfe.commons

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

@Throws(IOException::class)
fun InputStream.copyTo(out: OutputStream, bufferSize: Int = DEFAULT_BUFFER_SIZE, length: Long = this.available().toLong(), listener: ((Float) -> Unit)? = null): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
        listener?.invoke(((bytesCopied * 100) / length).toFloat())
    }
    return bytesCopied
}
