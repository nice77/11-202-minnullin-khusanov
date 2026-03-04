package ru.info.search

import kotlin.math.log10

/**
 * Считает Inverse Document Frequency для токенов и лемм.
 */
class IdfCalculator(private val data: CollectedData) {

    private val tokenDF: Map<String, Int> by lazy { computeTokenDF() }

    private val lemmaDF: Map<String, Int> by lazy { computeLemmaDF() }

    /**
     * IDF для конкретного токена.
     */
    fun getTokenIdf(token: String): Double {
        val df = tokenDF.getOrDefault(token, 0)
        return if (df > 0) log10(data.totalDocs.toDouble() / df) else 0.0
    }

    /**
     * IDF для конкретной леммы.
     */
    fun getLemmaIdf(lemma: String): Double {
        val df = lemmaDF.getOrDefault(lemma, 0)
        return if (df > 0) log10(data.totalDocs.toDouble() / df) else 0.0
    }

    /**
     * Document Frequency для токенов:
     * в скольких документах встречается каждый токен.
     */
    private fun computeTokenDF(): Map<String, Int> {
        val df = mutableMapOf<String, Int>()
        for (token in data.allTokens) {
            df[token] = 0
        }
        for (doc in data.documentsTokenized) {
            val uniqueInDoc = doc.toSet()
            for (token in data.allTokens) {
                if (token in uniqueInDoc) {
                    df[token] = df.getOrDefault(token, 0) + 1
                }
            }
        }
        return df
    }

    /**
     * Document Frequency для лемм:
     * приоритетно берётся из invert-index.txt, иначе считается
     * по наличию хотя бы одной формы в документе.
     */
    private fun computeLemmaDF(): Map<String, Int> {
        // Если index.txt заполнен, используем его
        if (data.lemmaIndexDF.isNotEmpty()) {
            // Дополняем вычисленными значениями для тех лемм,
            // которых нет в index.txt
            val computed = computeLemmaDFFromDocs()
            return data.allLemmas.associateWith { lemma ->
                data.lemmaIndexDF[lemma] ?: computed.getOrDefault(lemma, 0)
            }
        }
        return computeLemmaDFFromDocs()
    }

    private fun computeLemmaDFFromDocs(): Map<String, Int> {
        val df = mutableMapOf<String, Int>()
        for (lemma in data.allLemmas) {
            df[lemma] = 0
        }
        for (doc in data.documentsTokenized) {
            val uniqueInDoc = doc.toSet()
            for (lemma in data.allLemmas) {
                val forms = data.lemmaToTokens[lemma] ?: continue
                if (forms.any { it in uniqueInDoc }) {
                    df[lemma] = df.getOrDefault(lemma, 0) + 1
                }
            }
        }
        return df
    }
}
