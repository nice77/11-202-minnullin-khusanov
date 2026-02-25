package ru.info.search

/**
 * Считает Term Frequency для токенов и лемм.
 *
 * TF(token) = count(token, doc) / total_words_in_doc
 * TF(lemma) = sum(count(form_i, doc) for all forms) / total_words_in_doc
 */
class TfCalculator(private val data: CollectedData) {

    /**
     * TF каждого токена в документе docIndex.
     */
    fun computeTokenTf(docIndex: Int): Map<String, Double> {
        val docTokens = data.documentsTokenized[docIndex]
        val totalWords = docTokens.size
        if (totalWords == 0) return emptyMap()

        val tokenCounts = data.docsTokenCounts[docIndex]
        val result = mutableMapOf<String, Double>()

        for ((token, count) in tokenCounts) {
            result[token] = count.toDouble() / totalWords
        }
        return result
    }

    /**
     * TF каждой леммы в документе docIndex.
     */
    fun computeLemmaTf(docIndex: Int): Map<String, Double> {
        val docTokens = data.documentsTokenized[docIndex]
        val totalWords = docTokens.size
        if (totalWords == 0) return emptyMap()

        val tokenCounts = data.docsTokenCounts[docIndex]
        val result = mutableMapOf<String, Double>()

        for (lemma in data.allLemmas) {
            val forms = data.lemmaToTokens[lemma] ?: continue
            val sumOccurrences = forms.sumOf { tokenCounts.getOrDefault(it, 0) }
            if (sumOccurrences > 0) {
                result[lemma] = sumOccurrences.toDouble() / totalWords
            }
        }
        return result
    }
}
