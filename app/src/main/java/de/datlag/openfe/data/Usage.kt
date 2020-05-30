package de.datlag.openfe.data

import java.io.File

data class Usage(
    val file: File,
    val max: Long,
    val current: Long,
    val percentage: Float
)