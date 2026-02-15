package ru.info.search

import java.io.File

internal fun loadInvertedIndex(path: String): Map<String, Set<Int>> {
    return File(path).readLines().associate { line ->
        val parts = line.split(" ")
        val lemma = parts[0]
        val docIds = parts.subList(1, parts.size).map { it.toInt() }.toSet()
        lemma to docIds
    }
}

internal fun loadTokenToLemma(path: String): Map<String, String> {
    val map = mutableMapOf<String, String>()
    File(path).readLines().forEach { line ->
        val parts = line.split(" ")
        if (parts.isNotEmpty()) {
            val lemma = parts[0]
            parts.forEach { token -> map[token] = lemma }
        }
    }
    return map
}