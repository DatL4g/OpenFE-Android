package de.datlag.openfe.commons

import android.os.Build

fun androidGreaterOr(version: Int): Boolean = Build.VERSION.SDK_INT >= version