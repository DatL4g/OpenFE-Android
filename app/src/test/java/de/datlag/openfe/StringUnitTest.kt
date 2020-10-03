package de.datlag.openfe

import de.datlag.openfe.commons.isNotCleared
import de.datlag.openfe.commons.toLower
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.contracts.ExperimentalContracts

class StringUnitTest {

    @ExperimentalContracts
    @Test
    fun checkStringNotEmpty() {
        val testString = String()

        assertEquals(testString.isNotCleared(), false)
    }

    @Test
    fun checkStringToLowerCase() {
        val testString = "LoReM iPsUm dOloR sIt amEt, conSEctEtUr aDipIsiCi eLit"
        val actualString = "lorem ipsum dolor sit amet, consectetur adipisici elit"

        assertEquals(testString.toLower(), actualString)
    }
}
