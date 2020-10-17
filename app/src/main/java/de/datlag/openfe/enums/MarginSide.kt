package de.datlag.openfe.enums

import io.michaelrocks.paranoid.Obfuscate

@Obfuscate
enum class MarginSide {
    TOP,
    LEFT,
    RIGHT,
    BOTTOM;

    companion object {
        fun horizontal(): Array<MarginSide> = arrayOf(LEFT, RIGHT)

        fun vertical(): Array<MarginSide> = arrayOf(TOP, BOTTOM)
    }
}
