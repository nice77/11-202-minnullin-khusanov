package ru.info.search

import java.io.File

data class IndexData(
    val invertedIndex: Map<String, Set<Int>>,
    val allDocIds: Set<Int>,
    val vocabulary: List<String>,
    val formToLemma: Map<String, String>,
    val tfidfByDoc: Map<Int, Map<String, Double>>
)

class DataLoader(
    private val invertedIndexFile: File,
    private val lemmasFile: File?,
    private val tfidfDir: File?
) {
    fun load(): IndexData {
        val invertedIndex = loadInvertedIndex()
        val allDocIds = invertedIndex.values.flatten().toSortedSet()
        val vocabulary = invertedIndex.keys.sorted()
        val formToLemma = loadFormToLemma(vocabulary)
        val tfidfByDoc = loadTfidf(allDocIds)

        println("Loaded ${vocabulary.size} lemmas, ${allDocIds.size} documents, ${formToLemma.size} word forms")
        if (tfidfByDoc.isNotEmpty()) {
            println("Loaded TF-IDF for ${tfidfByDoc.size} documents from files")
        } else {
            println("No TF-IDF files found, will use binary IDF model")
        }

        return IndexData(invertedIndex, allDocIds, vocabulary, formToLemma, tfidfByDoc)
    }

    private fun loadInvertedIndex(): Map<String, Set<Int>> {
        val result = mutableMapOf<String, Set<Int>>()
        invertedIndexFile.forEachLine { line ->
            val parts = line.trim().split("\\s+".toRegex())
            if (parts.isNotEmpty()) {
                val lemma = parts[0].lowercase()
                val docIds = parts.drop(1).mapNotNull { it.toIntOrNull() }.toSet()
                result[lemma] = docIds
            }
        }
        return result
    }

    private fun loadFormToLemma(vocabulary: List<String>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for (lemma in vocabulary) {
            result[lemma] = lemma
        }
        if (lemmasFile != null && lemmasFile.exists()) {
            lemmasFile.forEachLine { line ->
                val parts = line.trim().split("\\s+".toRegex())
                if (parts.size >= 2) {
                    val lemma = parts[0].lowercase()
                    if (lemma in result) {
                        for (form in parts) {
                            result[form.lowercase()] = lemma
                        }
                    }
                }
            }
        }
        return result
    }

    private fun loadTfidf(allDocIds: Set<Int>): Map<Int, Map<String, Double>> {
        if (tfidfDir == null || !tfidfDir.exists() || !tfidfDir.isDirectory) {
            return emptyMap()
        }

        val result = mutableMapOf<Int, Map<String, Double>>()

        for (docId in allDocIds) {
            val file = File(tfidfDir, "doc_${docId + 1}_lemmas.txt") ?: continue
            val entries = mutableMapOf<String, Double>()

            file.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.isNotBlank()) {
                    val parts = trimmed.split("\\s+".toRegex())
                    if (parts.size >= 3) {
                        val lemma = parts[0].lowercase()
                        val tfidf = parseDouble(parts[2])
                        if (tfidf > 0.0) {
                            entries[lemma] = tfidf
                        }
                    }
                }
            }

            if (entries.isNotEmpty()) {
                result[docId] = entries
            }
        }

        return result
    }

    /**
     * Парсит число, поддерживая и запятую и точку как дробный разделитель.
     */
    private fun parseDouble(s: String): Double {
        return s.replace(',', '.').toDoubleOrNull() ?: 0.0
    }
}
