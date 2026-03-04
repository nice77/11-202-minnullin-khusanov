package ru.info.search

import java.io.File

/**
 * Хранит все распарсенные и подготовленные данные,
 * необходимые для последующего подсчёта метрик.
 */
data class CollectedData(
    /** Токенизированные документы: список слов каждого документа */
    val documentsTokenized: List<List<String>>,
    /** Общее количество документов */
    val totalDocs: Int,
    /** Все известные токены (все формы всех лемм) */
    val allTokens: Set<String>,
    /** Все леммы */
    val allLemmas: Set<String>,
    /** Лемма -> список её форм */
    val lemmaToTokens: Map<String, List<String>>,
    /** Токен -> его лемма */
    val tokenToLemma: Map<String, String>,
    /**
     * Для каждого документа: Map<токен, количество вхождений>
     * Считаются только токены из allTokens
     */
    val docsTokenCounts: List<Map<String, Int>>,
    /**
     * DF из index.txt (лемма -> в скольких документах встречается)
     * Используется как fallback / основной источник DF для лемм
     */
    val lemmaIndexDF: Map<String, Int>
)

/**
 * Собирает и подготавливает все исходные данные:
 * парсит документы, леммы, индекс, токенизирует.
 */
class DocumentDataCollector(
    private val documentsFile: File,
    private val lemmasFile: File,
    private val indexFile: File
) {
    companion object {
        private const val DOC_SEPARATOR =
            "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">"
    }

    fun collect(): CollectedData {
        // 1. Разбиваем файл на документы
        val rawDocuments = splitDocuments()
        val totalDocs = rawDocuments.size
        println("Всего документов: $totalDocs")

        // 2. Парсим леммы
        val lemmaToTokens = parseLemmas()
        val allTokens = lemmaToTokens.values.flatten().toSet()
        val allLemmas = lemmaToTokens.keys
        val tokenToLemma = buildTokenToLemmaMap(lemmaToTokens)

        // 3. Токенизируем документы
        val documentsTokenized = rawDocuments.map { html -> tokenizeHtml(html) }

        // 4. Для каждого документа считаем вхождения известных токенов
        val docsTokenCounts = documentsTokenized.map { docTokens ->
            countOccurrences(docTokens, allTokens)
        }

        // 5. Парсим индексный файл
        val lemmaIndexDF = parseIndex()

        return CollectedData(
            documentsTokenized = documentsTokenized,
            totalDocs = totalDocs,
            allTokens = allTokens,
            allLemmas = allLemmas,
            lemmaToTokens = lemmaToTokens,
            tokenToLemma = tokenToLemma,
            docsTokenCounts = docsTokenCounts,
            lemmaIndexDF = lemmaIndexDF
        )
    }

    private fun splitDocuments(): List<String> {
        val content = documentsFile.readText()
        return content.split(DOC_SEPARATOR)
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun parseLemmas(): Map<String, List<String>> {
        val result = mutableMapOf<String, List<String>>()
        lemmasFile.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank()) {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.isNotEmpty()) {
                    val lemma = parts[0].lowercase()
                    val tokens = parts.drop(1).map { it.lowercase() }
                    result[lemma] = tokens.ifEmpty { listOf(lemma) }
                }
            }
        }
        return result
    }

    private fun parseIndex(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        if (!indexFile.exists()) return result
        indexFile.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.isNotBlank()) {
                val parts = trimmed.split("\\s+".toRegex())
                if (parts.size >= 2) {
                    result[parts[0].lowercase()] = parts.size - 1
                }
            }
        }
        return result
    }

    private fun buildTokenToLemmaMap(lemmaToTokens: Map<String, List<String>>): Map<String, String> {
        val result = mutableMapOf<String, String>()
        for ((lemma, tokens) in lemmaToTokens) {
            for (token in tokens) {
                result[token] = lemma
            }
            result[lemma] = lemma
        }
        return result
    }

    private fun tokenizeHtml(html: String): List<String> {
        return html.lowercase()
            .split(Regex("[^а-яё]+"))
            .filter { it.isNotBlank() }
    }

    private fun countOccurrences(docTokens: List<String>, knownTokens: Set<String>): Map<String, Int> {
        val counts = mutableMapOf<String, Int>()
        for (token in docTokens) {
            if (token in knownTokens) {
                counts[token] = counts.getOrDefault(token, 0) + 1
            }
        }
        return counts
    }
}
