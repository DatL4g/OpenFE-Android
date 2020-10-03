package de.datlag.openfe.commons

fun <T> List<T>.copyOf(): List<T> {
    val original = this
    return mutableListOf<T>().apply { addAll(original) }
}

fun <T> List<T>.mutableCopyOf(): MutableList<T> {
    val original = this
    return mutableListOf<T>().apply { addAll(original) }
}
