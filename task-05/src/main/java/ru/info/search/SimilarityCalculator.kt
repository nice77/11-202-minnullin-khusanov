package ru.info.search

import kotlin.collections.iterator
import kotlin.math.sqrt

data class SearchResult(
    val docId: Int,
    val score: Double,
    val matchedLemmas: List<String>
)

class SimilarityCalculator {

    private fun cosine(a: DoubleArray, b: DoubleArray): Double {
        var dot = 0.0; var na = 0.0; var nb = 0.0
        for (i in a.indices) {
            dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i]
        }
        val d = sqrt(na) * sqrt(nb)
        return if (d == 0.0) 0.0 else dot / d
    }

    fun rank(
        queryVector: DoubleArray,
        documentVectors: Map<Int, DoubleArray>,
        queryLemmas: List<String>,
        invertedIndex: Map<String, Set<Int>>,
        topN: Int
    ): List<SearchResult> {
        val results = mutableListOf<SearchResult>()

        for ((docId, docVec) in documentVectors) {
            val score = cosine(queryVector, docVec)

            if (score <= 0.0) continue

            val matched = mutableListOf<String>()
            for (lemma in queryLemmas) {
                val docsWithLemma = invertedIndex[lemma] ?: continue
                if (docId in docsWithLemma) {
                    matched.add(lemma)
                }
            }

            results.add(SearchResult(docId, score, matched))
        }

        results.sortByDescending { it.score }

        if (results.size > topN) {
            return results.subList(0, topN)
        }

        return results
    }
}
