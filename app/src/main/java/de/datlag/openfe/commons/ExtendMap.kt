package de.datlag.openfe.commons

@Throws(IllegalArgumentException::class)
fun <K, V> MutableMap<K, V>.putAll(pair: Pair<Array<K>, Array<V>>) {
    if (pair.first.size != pair.second.size) {
        throw IllegalArgumentException("Cannot combine lists with dissimilar sizes")
    }

    for (position in pair.first.indices) {
        this[pair.first[position]] = pair.second[position]
    }
}

fun <K, V> mapOf(pair: Pair<Array<K>, Array<V>>): Map<K, V> {
    val mutableMap = mutableMapOf<K, V>()
    mutableMap.putAll(pair)
    return mutableMap.toMap()
}