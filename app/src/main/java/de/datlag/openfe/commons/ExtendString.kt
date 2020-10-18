@file:Obfuscate
package de.datlag.openfe.commons

import io.michaelrocks.paranoid.Obfuscate
import java.util.Locale

fun String.toLower() = this.toLowerCase(Locale.getDefault())

fun String.replaceLast(oldValue: String, newValue: String, ignoreCase: Boolean = false): String {
    val index = this.lastIndexOf(oldValue, ignoreCase = ignoreCase)
    return if (index < 0) this else this.replaceRange(index, index + oldValue.length, newValue)
}
