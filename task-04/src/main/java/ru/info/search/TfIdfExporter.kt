package ru.info.search

import java.io.File

/**
 * Результат TF-IDF для одного термина/леммы.
 */
data class TfIdfEntry(
    val term: String,
    val idf: Double,
    val tfIdf: Double
)

/**
 * Экспортирует результаты TF-IDF в текстовые файлы.
 *
 * Формат строки: <термин> <idf> <tf-idf>
 */
class TfIdfExporter(
    private val tokensOutputDir: File,
    private val lemmasOutputDir: File
) {
    init {
        tokensOutputDir.mkdirs()
        lemmasOutputDir.mkdirs()
    }

    /**
     * Записывает TF-IDF токенов для одного документа.
     */
    fun exportTokens(docIndex: Int, entries: List<TfIdfEntry>) {
        val file = File(tokensOutputDir, "doc_${docIndex + 1}_tokens.txt")
        writeEntries(file, entries)
        println("  → Записано ${entries.size} токенов в ${file.name}")
    }

    /**
     * Записывает TF-IDF лемм для одного документа.
     */
    fun exportLemmas(docIndex: Int, entries: List<TfIdfEntry>) {
        val file = File(lemmasOutputDir, "doc_${docIndex + 1}_lemmas.txt")
        writeEntries(file, entries)
        println("  → Записано ${entries.size} лемм в ${file.name}")
    }

    private fun writeEntries(file: File, entries: List<TfIdfEntry>) {
        file.printWriter().use { writer ->
            entries
                .sortedBy { it.term }
                .forEach { entry ->
                    writer.println(
                        "${entry.term} ${"%.6f".format(entry.idf)} ${"%.6f".format(entry.tfIdf)}"
                    )
                }
        }
    }
}
