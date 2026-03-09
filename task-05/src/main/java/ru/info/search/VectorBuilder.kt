package ru.info.search

import kotlin.collections.iterator
import kotlin.math.log10

data class QueryVectorResult(
    val vector: DoubleArray,
    val resolvedLemmas: List<String>,
    val unknownTokens: List<String>
)

class VectorBuilder(private val data: IndexData) {

    private val lemmaIndex = data.vocabulary.withIndex().associate { (i, l) -> l to i }
    private val vectorSize = data.vocabulary.size
    private val totalDocs = data.allDocIds.size.toDouble()

    private val idfCache = data.vocabulary.associateWith { lemma ->
        val df = data.invertedIndex[lemma]?.size ?: 0
        if (df > 0) log10(totalDocs / df) else 0.0
    }

    fun buildDocumentVector(docId: Int): DoubleArray {
        val vector = DoubleArray(vectorSize)

        val tfidf = data.tfidfByDoc[docId] ?: return vector
        for ((lemma, weight) in tfidf) {
            val idx = lemmaIndex[lemma] ?: continue
            vector[idx] = weight
        }

        return vector
    }

    fun buildAllDocumentVectors(): Map<Int, DoubleArray> {
        return data.allDocIds.associateWith { buildDocumentVector(it) }
    }

    fun buildQueryVector(queryTokens: List<String>): QueryVectorResult {
        val vector = DoubleArray(vectorSize)
        val resolved = mutableListOf<String>()
        val unknown = mutableListOf<String>()

        for (token in queryTokens) {
            val lemma = data.formToLemma[token]
            if (lemma != null && lemma in lemmaIndex) resolved.add(lemma)
            else unknown.add(token)
        }

        if (resolved.isEmpty()) return QueryVectorResult(vector, emptyList(), unknown)

        val counts = resolved.groupingBy { it }.eachCount()
        val total = resolved.size.toDouble()

        for ((lemma, count) in counts) {
            val idx = lemmaIndex[lemma] ?: continue
            val tf = count / total
            val idf = idfCache[lemma] ?: 0.0
            vector[idx] = tf * idf
        }

        return QueryVectorResult(vector, resolved.distinct(), unknown)
    }
}
