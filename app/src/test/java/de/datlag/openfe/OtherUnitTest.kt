package de.datlag.openfe

import de.datlag.openfe.util.toHumanReadable
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class OtherUnitTest {

    @Test
    fun humanReadableByteSize() {
        val size: Long = 2048

        assertEquals(size.toHumanReadable(true), "2,0 KiB")
        assertEquals(size.toHumanReadable(false), "2,0 kB")
    }

}
