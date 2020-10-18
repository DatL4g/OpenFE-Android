@file:Obfuscate
package de.datlag.openfe.commons

import android.os.Build
import io.michaelrocks.paranoid.Obfuscate

fun androidGreaterOr(version: Int): Boolean = Build.VERSION.SDK_INT >= version
