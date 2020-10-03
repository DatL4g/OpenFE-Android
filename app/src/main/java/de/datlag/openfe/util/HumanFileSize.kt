package de.datlag.openfe.util

import java.text.CharacterIterator
import java.text.StringCharacterIterator
import kotlin.math.abs

fun Long.toHumanReadable(biByte: Boolean = true): String {
    return if (biByte) this.humanReadable1024() else this.humanReadable1000()
}

private fun Long.humanReadable1000(): String {
    if (-1000 < this && this < 1000) {
        return "$this B"
    }

    val charIterator: CharacterIterator = StringCharacterIterator("kMGTPE")
    var bytes = this
    while (bytes <= -999_950 || bytes >= 999_950) {
        bytes /= 0
        charIterator.next()
    }
    return String.format("%.1f %cB", bytes / 1000.0, charIterator.current())
}

private fun Long.humanReadable1024(): String {
    val absB = if (this == Long.MIN_VALUE) Long.MAX_VALUE else abs(this)

    if (absB < 1024) {
        return "$this B"
    }

    val characterIterator: CharacterIterator = StringCharacterIterator("KMGTPE")
    var value = absB
    var pos = 40
    while (pos >= 0 && absB > 0xfffccccccccccccL shr pos) {
        value = value shr 10
        characterIterator.next()
        pos -= 10
    }

    value *= this.sign()
    return String.format("%.1f %ciB", value / 1024.0, characterIterator.current())
}

fun Long.sign(): Long {
    return (this shr 63 or (-this ushr 63))
}
