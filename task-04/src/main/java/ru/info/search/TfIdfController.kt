package ru.info.search

import java.io.File

/**
 * Управляет процессом:
 * 1. Сбор данных (DocumentDataCollector)
 * 2. Подсчёт TF (TfCalculator)
 * 3. Подсчёт IDF (IdfCalculator)
 * 4. Экспорт результатов (TfIdfExporter)
 */
class TfIdfController(
    documentsFile: File,
    lemmasFile: File,
    indexFile: File,
    tokensOutputDir: File,
    lemmasOutputDir: File
) {
    private val collector = DocumentDataCollector(documentsFile, lemmasFile, indexFile)
    private val exporter = TfIdfExporter(tokensOutputDir, lemmasOutputDir)

    fun run() {
        println("=== Этап 1: Сбор данных ===")
        val data = collector.collect()

        println("\n=== Этап 2: Инициализация калькуляторов ===")
        val tfCalc = TfCalculator(data)
        val idfCalc = IdfCalculator(data)

        println("Токенов: ${data.allTokens.size}, Лемм: ${data.allLemmas.size}")

        println("\n=== Этап 3: Подсчёт TF-IDF и экспорт ===")
        for (docIndex in data.documentsTokenized.indices) {
            val docTokens = data.documentsTokenized[docIndex]
            if (docTokens.isEmpty()) {
                println("Документ ${docIndex + 1}: пуст, пропускаем")
                continue
            }

            println("Документ ${docIndex + 1}:")

            // --- Токены ---
            val tokenTf = tfCalc.computeTokenTf(docIndex)
            val tokenEntries = tokenTf.map { (token, tf) ->
                val idf = idfCalc.getTokenIdf(token)
                TfIdfEntry(term = token, idf = idf, tfIdf = tf * idf)
            }
            exporter.exportTokens(docIndex, tokenEntries)

            // --- Леммы ---
            val lemmaTf = tfCalc.computeLemmaTf(docIndex)
            val lemmaEntries = lemmaTf.map { (lemma, tf) ->
                val idf = idfCalc.getLemmaIdf(lemma)
                TfIdfEntry(term = lemma, idf = idf, tfIdf = tf * idf)
            }
            exporter.exportLemmas(docIndex, lemmaEntries)
        }

        println("\n=== Готово! ===")
    }
}
