package de.datlag.openfe

import de.datlag.openfe.util.toHumanReadable
import org.junit.Assert.*
import org.junit.Test


class OtherUnitTest {

    @Test
    fun humanReadableByteSize() {
        val size: Long = 2048
        val biByteActual: String = "2,0 KiB"
        val nonBiByteActual: String = "2,0 kB"

        assertEquals(size.toHumanReadable(true), biByteActual)
        assertEquals(size.toHumanReadable(false), nonBiByteActual)
    }

}
